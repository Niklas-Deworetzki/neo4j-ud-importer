package se.gu.conll_u

import se.gu.ExtendedSyntax.ifPresent
import java.io.*

class CoNLLUReader(private val reader: BufferedReader) : Closeable, Iterator<CoNLLUEvent> {

    constructor(file: File) : this(FileReader(file).buffered())

    override fun close() =
        reader.close()

    private var nextLine: String? = readLine()
    var lineNumber: Long = 0
        private set

    private fun readLine(): String? {
        lineNumber++
        return reader.readLine()
    }

    companion object {
        val COLUMN_SEPARATOR = "\t+".toRegex()

        val METADATA = """^#\s+([^\s=]+)\s+=\s+(.+)$""".toRegex()
        val NEW_DOC = """^#\s+newdoc\s*(?:id\s+=\s+(.+))?$""".toRegex()
        val NEW_PAR = """^#\s+newpar\s*(?:id\s+=\s+(.+))?$""".toRegex()

        val COLUMNS = lookupMapFromList(CoNLLUFormat.COLUMNS)

        val ID_COLUMN_INDEX = COLUMNS["ID"]

        private fun lookupMapFromList(list: List<String>): Map<String, Int> =
            list.withIndex().associate { it.value to it.index }
    }

    private var nextEvent: CoNLLUEvent? = parseNextEvent()

    private fun parseNextEvent(): CoNLLUEvent? {
        if (nextLine == null) return null
        if (nextLine!!.startsWith('#')) {
            return extractSentenceBoundary()
        } else if (nextLine!!.isBlank()) {
            nextLine = readLine()
            return parseNextEvent()
        } else {
            return extractWordLine()
        }
    }


    override fun hasNext(): Boolean =
        nextEvent != null

    override fun next(): CoNLLUEvent {
        val result = nextEvent ?: throw NoSuchElementException()
        nextEvent = parseNextEvent()
        return result
    }

    private fun extractSentenceBoundary(): CoNLLUEvent {
        var startsDocument = false
        var documentId: String? = null
        var startsParagraph = false
        var paragraphId: String? = null
        val metadata = mutableMapOf<String, String>()

        do {
            val documentMatch = NEW_DOC.matchEntire(nextLine!!)
            if (documentMatch != null) {
                startsDocument = true
                documentId = documentMatch.groups[1]?.value

            } else {
                val paragraphMatch = NEW_PAR.matchEntire(nextLine!!)
                if (paragraphMatch != null) {
                    startsParagraph = true
                    paragraphId = paragraphMatch.groups[1]?.value

                } else {
                    val metadataMatch = METADATA.matchEntire(nextLine!!)
                    if (metadataMatch != null) {
                        val key = metadataMatch.groups[1]!!.value
                        val value = metadataMatch.groups[2]!!.value
                        metadata[key] = value
                    }
                }
            }

            nextLine = readLine()
        } while (nextLine != null && nextLine!!.startsWith('#'))
        return SentenceBoundary(startsDocument, documentId, startsParagraph, paragraphId, metadata)
    }

    private fun extractWordLine(): CoNLLUEvent.WordLine {
        val columns = nextLine!!.split(COLUMN_SEPARATOR)
        assert(columns.size == COLUMNS.size) {
            "Word line should have ${COLUMNS.size} columns but has ${columns.size}: $nextLine "
        }

        nextLine = readLine()

        val id = optionalGet(columns, ID_COLUMN_INDEX)
        if (id != null) {
            val indexOfRange = id.indexOf('-')
            if (indexOfRange > 0) {
                val from = id.substring(0, indexOfRange).toLong()
                val to = id.substring(indexOfRange + 1).toLong()
                return MultiWordToken(from, to, columns)
            }
            // TODO: Check for empty node (with ID like 5.3, required DEPS, no HEAD/DEPREL)
        }
        return Word(columns)
    }

    private class SentenceBoundary(
        override val isNewDocument: Boolean,
        override val documentId: String?,
        override val isNewParagraph: Boolean,
        override val paragraphId: String?,
        override val metadata: Map<String, String>,
    ) : CoNLLUEvent.SentenceBoundary {
        override fun toString(): String {
            val repr = StringBuilder()
            if (isNewDocument) {
                repr.append("# newdoc")
                documentId.ifPresent { repr.append(" id ").append(it) }
                repr.append('\n')
            }
            if (isNewParagraph) {
                repr.append("# newpar")
                paragraphId.ifPresent { repr.append(" id ").append(it) }
                repr.append('\n')
            }
            for ((key, value) in metadata) {
                repr.append("# $key = $value\n")
            }
            return repr.toString()
        }
    }

    private open class Line(
        val columns: List<String>
    ) {
        fun column(name: String): String? {
            val index = COLUMNS[name] ?: return null
            return columns[index]
        }

        override fun toString(): String =
            columns.joinToString("\t")
    }

    private class Word(
        columns: List<String>
    ) : Line(columns), CoNLLUEvent.Word

    private class MultiWordToken(
        override val fromId: Long,
        override val toId: Long,
        columns: List<String>
    ) : Line(columns), CoNLLUEvent.MultiWordToken

    private fun <V> optionalGet(list: List<V>, key: Int?): V? =
        if (key == null) null else list[key]
}
