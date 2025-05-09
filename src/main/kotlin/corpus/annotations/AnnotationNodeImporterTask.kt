package se.gu.corpus.annotations

import corpus.prepare.CorpusStatistics
import se.gu.corpus.Encoding
import se.gu.processor.Annotations
import se.gu.processor.CorpusTask

class AnnotationNodeImporterTask(statistics: CorpusStatistics) : CorpusTask<Unit>(statistics) {
    private lateinit var inventoryBuilder: InventoryBuilder

    private lateinit var includedRegions: Set<String>
    private lateinit var ignoredRegionAttributes: Set<String>

    override fun getValue(): Unit = Unit

    override fun setup() {
        this.inventoryBuilder = InventoryBuilder(configuration.databaseConnection)
        this.includedRegions = Encoding.includedRegions(configuration)
        this.ignoredRegionAttributes =
            if (configuration.annotationOptions.asNodes) emptySet()
            else Encoding.REGION_ANNOTATIONS_STORED_AS_PROPERTY
    }

    override fun teardown() {
        this.inventoryBuilder.close()
    }

    override fun processWord(annotations: Annotations) {
        for (stringValueAttribute in Encoding.COLUMNS_ENCODING_STRING) {
            val value = annotations.getString(stringValueAttribute)!!
            inventoryBuilder.addToInventory(stringValueAttribute, value)
        }

        for (mapValueAttribute in Encoding.COLUMNS_ENCODING_MAP) {
            val entries = annotations.getMap(mapValueAttribute)!!
            for ((key, value) in entries) {
                inventoryBuilder.addToInventory(key, value)
            }
        }
    }

    override fun enterRegion(region: String, annotations: Annotations) {
        if (region !in includedRegions) {
            return
        }

        for (attribute in annotations.keys()) {
            if (attribute !in ignoredRegionAttributes) {
                val value = annotations.getString(attribute)!!
                inventoryBuilder.addToInventory(attribute, value)
            }
        }
    }
}
