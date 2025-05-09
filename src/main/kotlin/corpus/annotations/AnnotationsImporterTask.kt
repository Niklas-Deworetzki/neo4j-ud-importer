package se.gu.corpus.annotations

import corpus.prepare.CorpusStatistics
import se.gu.corpus.Encoding
import se.gu.corpus.Encoding.INVENTORY_PROPERTY
import se.gu.corpus.Encoding.POSITION_PROPERTY
import se.gu.corpus.Encoding.REGION_LEFT_BOUND_PROPERTY
import se.gu.corpus.Encoding.WORD_NODE_LABEL
import se.gu.neo4j.BufferedQuery
import se.gu.neo4j.NamingConventions
import se.gu.processor.Annotations
import se.gu.processor.CorpusTask

typealias DelayedQuery = (Long, Map<String, Any?>) -> Unit

class AnnotationsImporterTask(statistics: CorpusStatistics) : CorpusTask<Unit>(statistics) {
    // Collect a pool of queries to commit at the end of this task.
    private val managedQueries = mutableListOf<BufferedQuery>()
    private fun managedQuery(query: String): BufferedQuery =
        configuration.databaseConnection.prepareBufferedQuery(query, "batch")
            .also { managedQueries.add(it) }


    override fun getValue() = Unit

    private lateinit var persistOnWordAsPropertyQuery: DelayedQuery
    private val persistOnRegionAsPropertyQueries = mutableMapOf<String, DelayedQuery>()
    private val inventories = mutableMapOf<String, Inventory>()

    private lateinit var includedRegions: Set<String>

    override fun processWord(annotations: Annotations) {
        val annotationData = mutableMapOf<String, Any?>()

        for (stringValueAttribute in Encoding.COLUMNS_ENCODING_STRING) {
            val attributeKey = NamingConventions.toPropertyName(stringValueAttribute)
            annotationData[attributeKey] = annotations.getString(stringValueAttribute)
        }

        for (mapValueAttribute in Encoding.COLUMNS_ENCODING_MAP) {
            annotationData.putAll(annotations.getMap(mapValueAttribute)!!)
        }

        if (configuration.annotationOptions.asProperties) {
            persistOnWordAsPropertyQuery(corpusPosition, annotationData)
        } else {
            annotationData.forEach { (key, value) ->
                inventories.getOrPut(key) { Inventory(key) }
                    .addWordValue(corpusPosition, value)
            }
        }
    }


    override fun enterRegion(region: String, annotations: Annotations) {
        if (region !in includedRegions) {
            return
        }

        val inventoryAnnotations = mutableMapOf<String, Any?>()
        val propertyAnnotations = mutableMapOf<String, Any?>()

        if (configuration.annotationOptions.asProperties) {
            // put everything as properties
            for (key in annotations.keys()) {
                val propertyName = NamingConventions.toPropertyName(key)
                propertyAnnotations[propertyName] = annotations.getString(key)
            }
        } else if (configuration.annotationOptions.asNodes) {
            // put everything as relationship to nodes
            for (key in annotations.keys()) {
                inventoryAnnotations[key] = annotations.getString(key)
            }
        } else {
            // put most stuff on nodes, but keep big and unique values as property.
            for (key in annotations.keys()) {
                if (key in Encoding.REGION_ANNOTATIONS_STORED_AS_PROPERTY) {
                    val propertyName = NamingConventions.toPropertyName(key)
                    propertyAnnotations[propertyName] = annotations.getString(key)
                } else {
                    inventoryAnnotations[key] = annotations.getString(key)
                }
            }
        }

        if (propertyAnnotations.isNotEmpty()) {
            getPropertyQueryFor(region)(corpusPosition, propertyAnnotations)
        }
        if (inventoryAnnotations.isNotEmpty()) {
            inventoryAnnotations.forEach { (key, value) ->
                inventories.getOrPut(key) { Inventory(key) }
                    .addRegionValue(region, corpusPosition, value)
            }
        }
    }


    override fun teardown() {
        this.managedQueries.forEach { it.forceCommit() }
    }

    override fun setup() {
        includedRegions = Encoding.includedRegions(configuration)

        val wordPropertyQuery = managedQuery(
            """
            UNWIND ${'$'}batch AS annotation
            WITH annotation
            MATCH (node:`${WORD_NODE_LABEL}` {`${POSITION_PROPERTY}`: annotation.word})
            SET node += annotation.properties 
            """
        )
        this.persistOnWordAsPropertyQuery = { position, properties ->
            val parameter = mapOf(
                "word" to position,
                "properties" to properties
            )
            wordPropertyQuery.submitParameter(parameter)
        }
    }

    private fun getPropertyQueryFor(region: String): DelayedQuery {
        return persistOnRegionAsPropertyQueries.getOrPut(region) {
            val normalizedName = NamingConventions.toNodeLabel(region)
            val query = managedQuery(
                """
                UNWIND ${'$'}batch AS annotation
                WITH annotation
                MATCH (node:`${normalizedName}` {`${REGION_LEFT_BOUND_PROPERTY}`: annotation.region})
                SET node += annotation.properties
                """
            )
            object : DelayedQuery {
                override fun invoke(position: Long, properties: Map<String, Any?>) {
                    val parameter = mapOf(
                        "region" to position,
                        "properties" to properties
                    )
                    query.submitParameter(parameter)
                }
            }
        }
    }

    private inner class Inventory(inventory: String) {
        private val nodeLabel: String = NamingConventions.toNodeLabel(inventory)
        private val relationshipType: String = NamingConventions.toRelationshipType(inventory)

        private val wordQuery = managedQuery(
            """
                 UNWIND ${'$'}batch AS annotation
                 WITH annotation
                 MATCH (word:`${WORD_NODE_LABEL}` {`${POSITION_PROPERTY}`: annotation.target})
                 MATCH (inventory:`${nodeLabel}` {`${INVENTORY_PROPERTY}`: annotation.value})
                 CREATE (word)-[:`${relationshipType}`]->(inventory)
            """
        )

        private val regionQueries = mutableMapOf<String, BufferedQuery>()
        private fun queryForRegion(region: String): BufferedQuery {
            val normalizedName = NamingConventions.toNodeLabel(region)
            return regionQueries.computeIfAbsent(region) {
                managedQuery(
                    """
                        UNWIND ${'$'}batch AS annotation
                        WITH annotation
                        MATCH (region:`${normalizedName}` {`${REGION_LEFT_BOUND_PROPERTY}`: annotation.target})
                        MATCH (inventory:`${nodeLabel}` {`${INVENTORY_PROPERTY}`: annotation.value})
                        CREATE (region)-[:`${relationshipType}`]->(inventory)
                        """
                )
            }
        }

        private fun submitToQuery(query: BufferedQuery, position: Long, value: Any?) {
            val queryParameters = mapOf(
                "target" to position,
                "value" to value
            )
            query.submitParameter(queryParameters)
        }


        fun addWordValue(corpusPosition: Long, value: Any?) {
            submitToQuery(wordQuery, corpusPosition, value)
        }

        fun addRegionValue(region: String, startPosition: Long, value: Any?) {
            submitToQuery(queryForRegion(region), startPosition, value)
        }
    }
}
