package corpus.regions

import corpus.prepare.CorpusStatistics
import se.gu.corpus.Encoding
import se.gu.processor.Annotations
import se.gu.processor.CorpusTask

class RegionImportTask(statistics: CorpusStatistics) : CorpusTask<Unit>(statistics) {
    private lateinit var builder: RegionBuilder

    private lateinit var includedRegions: Set<String>

    override fun setup() {
        this.builder = RegionBuilder(configuration.databaseConnection)
        includedRegions = Encoding.includedRegions(configuration)
    }

    override fun teardown() {
        this.builder.close()
    }

    override fun enterRegion(region: String, annotations: Annotations) {
        if (region in includedRegions) {
            builder.enter(region, corpusPosition)
        }
    }

    override fun exitRegion(region: String) {
        if (region in includedRegions) {
            builder.exit(region, corpusPosition - 1)
        }
    }

    override fun getValue() = Unit
}
