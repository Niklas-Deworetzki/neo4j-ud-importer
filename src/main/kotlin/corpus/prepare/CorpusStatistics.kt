package corpus.prepare

class CorpusStatistics(
    private val wordCounter: Counter,
    private val regionCounters: Map<String, Counter>,
    private val accumulatedRegionSizes: Map<String, Long>
) {
    val corpusSize: Long
        get() = wordCounter.occurrences()

    val containsDeps: Boolean
        get() = wordCounter.attributeOccurrences("DEPS") != 0L

    val knownRegions: Set<String>
        get() = regionCounters.keys

    fun occurrencesOfRegion(region: String): Long =
        regionCounters[region]?.occurrences() ?: 0L

    fun wordsCoveredByRegion(region: String): Long =
        accumulatedRegionSizes[region] ?: 0L
}
