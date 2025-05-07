package se.gu.conll_u

import se.gu.application.Configuration
import se.gu.neo4j.NamingConventions.CONLLU_REGION_DOCUMENT
import se.gu.neo4j.NamingConventions.CONLLU_REGION_MWT
import se.gu.neo4j.NamingConventions.CONLLU_REGION_PARAGRAPH
import se.gu.neo4j.NamingConventions.CONLLU_REGION_SENTENCE
import se.gu.processor.CorpusTask
import se.gu.processor.Processor
import java.io.File

class CoNLLUProcessor(override val configuration: Configuration) : Processor(configuration) {
    private val corpusFiles = configuration.allCorpusFiles()

    override fun applyToCorpus(corpusTask: CorpusTask<*>) {
        val state = ProcessState(corpusTask)
        for (corpusFile in corpusFiles) {
            CoNLLUReader(corpusFile).use { reader ->
                state.processDocument(reader, corpusFile)
            }
        }
    }

    private inner class ProcessState(private val task: CorpusTask<*>) {
        private var isInDocument = false
        private var isInParagraph = false

        fun processDocument(reader: CoNLLUReader, file: File) {
            val firstEvent = reader.next()
            enterRegions(firstEvent as CoNLLUEvent.SentenceBoundary)

            var endOfMultiWordToken = 0L
            var eventCausingException: CoNLLUEvent? = null
            try {
                for (event in reader) {
                    eventCausingException = event
                    when (event) {
                        is CoNLLUEvent.SentenceBoundary -> {
                            exitRegions(event)
                            enterRegions(event)
                        }

                        is CoNLLUEvent.Word -> {
                            task.processWord(CoNLLUAnnotations.WordLine(event))
                            task.corpusPosition++

                            if (task.corpusPosition == endOfMultiWordToken) {
                                task.exitRegion(CONLLU_REGION_MWT)
                            }
                        }

                        is CoNLLUEvent.MultiWordToken -> {
                            endOfMultiWordToken = task.corpusPosition + event.size
                            task.enterRegion(CONLLU_REGION_MWT, CoNLLUAnnotations.WordLine(event))
                        }
                    }
                }
                exitAllRegions()
            } catch (ex: Exception) {
                throw CoNLLUParseException(eventCausingException, file, reader.lineNumber, ex)
            }
        }

        private fun enterRegions(event: CoNLLUEvent.SentenceBoundary) {
            if (event.isNewDocument) {
                isInDocument = true
                task.enterRegion(
                    CONLLU_REGION_DOCUMENT,
                    CoNLLUAnnotations.DocumentOrParagraphBoundary(event.documentId)
                )
            }
            if (event.isNewParagraph) {
                isInParagraph = true
                task.enterRegion(
                    CONLLU_REGION_PARAGRAPH,
                    CoNLLUAnnotations.DocumentOrParagraphBoundary(event.paragraphId)
                )
            }

            task.enterRegion(CONLLU_REGION_SENTENCE, CoNLLUAnnotations.Sentence(event))
        }

        private fun exitRegions(event: CoNLLUEvent.SentenceBoundary) {
            task.exitRegion(CONLLU_REGION_SENTENCE)
            if (isInParagraph && (event.isNewDocument || event.isNewParagraph)) {
                isInParagraph = false
                task.exitRegion(CONLLU_REGION_PARAGRAPH)
            }
            if (isInDocument && event.isNewDocument) {
                isInDocument = false
                task.exitRegion(CONLLU_REGION_DOCUMENT)
            }
        }

        private fun exitAllRegions() {
            task.exitRegion(CONLLU_REGION_SENTENCE)
            if (isInParagraph) {
                isInParagraph = false
                task.exitRegion(CONLLU_REGION_PARAGRAPH)
            }
            if (isInDocument) {
                isInDocument = false
                task.exitRegion(CONLLU_REGION_DOCUMENT)
            }
        }
    }
}
