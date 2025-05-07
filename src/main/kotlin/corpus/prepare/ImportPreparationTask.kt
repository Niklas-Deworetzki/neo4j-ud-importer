package corpus.prepare

import se.gu.neo4j.NamingConventions
import se.gu.neo4j.NamingConventions.CONLLU_REGION_DOCUMENT
import se.gu.neo4j.NamingConventions.CONLLU_REGION_MWT
import se.gu.neo4j.NamingConventions.CONLLU_REGION_PARAGRAPH
import se.gu.processor.Annotations
import se.gu.processor.CorpusTask

class ImportPreparationTask : CorpusTask<CorpusStatistics>(0) {
    private val wordCounter = MutableCounter()
    private val regionCounters = HashMap<String, MutableCounter>()

    private val lastRegionStart = HashMap<String, Long>()
    private val accumulatedRegionSizes = HashMap<String, Long>()

    private lateinit var includedRegions: Set<String>

    override fun setup() {
        val includedRegions = mutableSetOf(NamingConventions.CONLLU_REGION_SENTENCE)
        if (configuration.encodingOptions.encodeMwts) {
            includedRegions.add(CONLLU_REGION_MWT)
        }
        if (configuration.encodingOptions.encodeParagraphs) {
            includedRegions.add(CONLLU_REGION_PARAGRAPH)
        }
        if (configuration.encodingOptions.encodeDocuments) {
            includedRegions.add(CONLLU_REGION_DOCUMENT)
        }
        this.includedRegions = includedRegions
    }

    override fun processWord(annotations: Annotations) {
        increment(wordCounter, annotations)
    }

    override fun enterRegion(region: String, annotations: Annotations) {
        if (region in includedRegions) {
            val counter = regionCounters.getOrPut(region) { MutableCounter() }
            increment(counter, annotations)
            lastRegionStart[region] = corpusPosition
        }
    }

    override fun exitRegion(region: String) {
        if (region in includedRegions) {
            val currentStart = lastRegionStart.remove(region) ?: 0L
            val currentSize = accumulatedRegionSizes[region] ?: 0L
            accumulatedRegionSizes[region] = currentSize + (corpusPosition - currentStart)
        }
    }

    private fun increment(counter: MutableCounter, attributes: Annotations) {
        counter.increment()
        attributes.keys().forEach(counter::incrementAttribute)
    }

    override fun getValue() = CorpusStatistics(wordCounter, regionCounters, accumulatedRegionSizes)
}
