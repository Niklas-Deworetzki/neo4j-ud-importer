package corpus.regions

import corpus.prepare.CorpusStatistics
import se.gu.neo4j.NamingConventions.CONLLU_REGION_DOCUMENT
import se.gu.neo4j.NamingConventions.CONLLU_REGION_MWT
import se.gu.neo4j.NamingConventions.CONLLU_REGION_PARAGRAPH
import se.gu.neo4j.NamingConventions.CONLLU_REGION_SENTENCE
import se.gu.processor.Annotations
import se.gu.processor.CorpusTask

class RegionImportTask(statistics: CorpusStatistics) : CorpusTask<Unit>(statistics) {
    private lateinit var builder: RegionBuilder

    private lateinit var importedRegions: Set<String>

    override fun setup() {
        this.builder = RegionBuilder(configuration.databaseConnection)

        val regions = mutableSetOf(CONLLU_REGION_SENTENCE)
        if (configuration.encodingOptions.encodeMwts) {
            regions.add(CONLLU_REGION_MWT)
        }
        if (configuration.encodingOptions.encodeDocuments) {
            regions.add(CONLLU_REGION_DOCUMENT)
        }
        if (configuration.encodingOptions.encodeParagraphs) {
            regions.add(CONLLU_REGION_PARAGRAPH)
        }
        importedRegions = regions.toSet()
    }

    override fun teardown() {
        this.builder.close()
    }

    override fun enterRegion(region: String, annotations: Annotations) {
        if (region in importedRegions) {
            builder.enter(region, corpusPosition)
        }
    }

    override fun exitRegion(region: String) {
        if (region in importedRegions) {
            builder.exit(region, corpusPosition - 1)
        }
    }

    override fun getValue() = Unit
}
