package se.gu.processor

import corpus.prepare.CorpusStatistics

abstract class CorpusTask<T>(private val knownCorpusSize: Long) : Task<T>() {
    constructor(statistics: CorpusStatistics): this(statistics.corpusSize)

    open fun processWord(annotations: Annotations) {}

    open fun enterRegion(region: String, annotations: Annotations) {}

    open fun exitRegion(region: String) {}

    /**
     * Managed by [Processor] executing this task.
     */
    var corpusPosition: Long = 0

    override val progress: Progress
        get() = Progress(corpusPosition, knownCorpusSize)
}
