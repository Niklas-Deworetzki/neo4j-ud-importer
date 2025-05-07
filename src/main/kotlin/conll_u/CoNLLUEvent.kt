package se.gu.conll_u

sealed interface CoNLLUEvent {

    interface SentenceBoundary : CoNLLUEvent {
        val isNewDocument: Boolean

        val documentId: String?

        val isNewParagraph: Boolean

        val paragraphId: String?

        val metadata: Map<String, String>
    }


    sealed interface WordLine : CoNLLUEvent {
        val columns: List<String>

        fun column(name: String): String?
    }

    interface Word : WordLine

    interface MultiWordToken : WordLine {
        val fromId: Long
        val toId: Long

        val size: Long get() = 1 + toId - fromId
    }
}
