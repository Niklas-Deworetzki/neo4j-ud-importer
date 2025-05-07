package se.gu.corpus.tree

import se.gu.neo4j.DatabaseConnection
import se.gu.neo4j.NamingConventions.DEPENDENCY_RELATION_PROPERTY
import se.gu.neo4j.NamingConventions.DEPENDENCY_RELATION_TYPE
import se.gu.neo4j.NamingConventions.POSITION_PROPERTY
import se.gu.neo4j.NamingConventions.ROOT_NODE_LABEL
import se.gu.neo4j.NamingConventions.WORD_NODE_LABEL
import se.gu.processor.Annotations
import java.io.Closeable

typealias Reference = String

class DepTreeBuilder(connection: DatabaseConnection) : Closeable {

    private val dependencyRelationQuery = connection.prepareBufferedQuery(
        """
            UNWIND ${'$'}batch AS edge WITH edge
            MATCH (src:`${WORD_NODE_LABEL}` {`${POSITION_PROPERTY}`: edge.src})
            MATCH (dst:`${WORD_NODE_LABEL}` {`${POSITION_PROPERTY}`: edge.dst})
            CREATE (src)-[r:`${DEPENDENCY_RELATION_TYPE}` {`${DEPENDENCY_RELATION_PROPERTY}`: edge.type}]->(dst)
        """,
        "batch"
    )

    private val rootNodesQuery = connection.prepareBufferedQuery(
        """
            UNWIND ${'$'}batch AS position WITH position
            MATCH (root:`${WORD_NODE_LABEL}` {`${POSITION_PROPERTY}`: position})
            SET root:`${ROOT_NODE_LABEL}`
        """,
        "batch"
    )

    private var startOfRegionInCorpus: Long = 0L

    private var rootIndex: Long = -1L
    private val wordToIndex = HashMap<Reference, Long>()
    private val indexToHead = HashMap<Long, Reference?>()
    private val indexToType = HashMap<Long, String?>()
    private var offsetInRegion: Long = 0L

    fun addWord(annotations: Annotations) {
        val index = offsetInRegion++
        val word = annotations.getString("ID") ?: return
        val head = annotations.getString("HEAD")
        val type = annotations.getString("DEPREL")

        if (head == "0") {
            rootIndex = index
        }

        wordToIndex[word] = index
        indexToHead[index] = head
        indexToType[index] = type
    }


    fun startNewSentence(atPosition: Long) {
        startOfRegionInCorpus = atPosition
        rootIndex = -1L
        offsetInRegion = 0

        listOf(wordToIndex, indexToHead, indexToType).forEach { it.clear() }
    }

    private fun convertToEdge(wordIndex: Long): Map<String, Any?>? {
        val head = indexToHead[wordIndex]
        val headIndex = wordToIndex[head]
        if (head == null || headIndex == null) return null

        val type = indexToType[wordIndex]

        return mapOf(
            "dst" to wordIndex + startOfRegionInCorpus,
            "src" to headIndex + startOfRegionInCorpus,
            "type" to type
        )
    }

    fun endSentence() {
        if (rootIndex == -1L)
            return

        val relations = indexToHead.keys
            .mapNotNullTo(ArrayList(indexToHead.size), ::convertToEdge)
        dependencyRelationQuery.submitParameters(relations)
        rootNodesQuery.submitParameter(rootIndex + startOfRegionInCorpus)
    }

    override fun close() {
        dependencyRelationQuery.forceCommit()
        rootNodesQuery.forceCommit()
    }
}
