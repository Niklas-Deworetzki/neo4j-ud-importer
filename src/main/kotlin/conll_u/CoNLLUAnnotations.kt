package se.gu.conll_u

import se.gu.ExtendedSyntax.combine
import se.gu.processor.Annotations
import se.gu.ExtendedSyntax.map

abstract class CoNLLUAnnotations : Annotations() {
    override fun getList(attribute: String): List<String>? =
        getString(attribute).map(CoNLLUFormat::parseList)

    override fun getMap(attribute: String): Iterable<Pair<String, String>>? =
        getString(attribute).map(CoNLLUFormat::parseMap)

    /**
     * Attributes for a document or paragraph boundary.
     *
     * Provides id associated with *newdoc* or *newpar*
     */
    class DocumentOrParagraphBoundary(
        private val id: String?
    ) : CoNLLUAnnotations() {
        override fun getString(attribute: String): String? =
            if (attribute == "id") id else null

        override fun keys(): Set<String> =
            if (id != null) setOf("id") else emptySet()
    }

    /**
     * Attributes for a sentence boundary.
     *
     * Provides all key-value-pairs provided in the sentence metadata.
     */
    class Sentence(
        private val sentence: CoNLLUEvent.SentenceBoundary
    ) : CoNLLUAnnotations() {
        override fun getString(attribute: String): String? =
            sentence.metadata[attribute]

        override fun keys(): Set<String> =
            sentence.metadata.keys
    }

    /**
     * Attributes for a line containing a word.
     *
     * Provides the document columns as keys with values for each column.
     */
    class WordLine(private val wordLine: CoNLLUEvent.WordLine) : CoNLLUAnnotations() {
        override fun getString(attribute: String): String? =
            wordLine.column(attribute)

        override fun keys(): Set<String> {
            val result = mutableSetOf<String>()
            combine(CoNLLUFormat.COLUMNS, wordLine.columns) { key, value ->
                if (key == "ID" || value != CoNLLUFormat.EMPTY_VALUE)
                    result.add(key)
            }
            return result
        }
    }
}
