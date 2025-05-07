package se.gu.corpus

object Encoding {
    /**
     * Attribute keys on structures that are encoded as properties by default.
     */
    val REGION_ANNOTATIONS_STORED_AS_PROPERTY = setOf(
        "sent_id", "id", "text", "text_en"
    )

    /**
     * Columns in .conllu file storing a string to be included as encoded annotations.
     */
    val COLUMNS_ENCODING_STRING = setOf(
        "FORM", "LEMMA", "UPOS", "XPOS"
    )

    /**
     * Columns in .conllu file storing key-value pairs to be included as encoded annotations.
     */
    val COLUMNS_ENCODING_MAP = setOf(
        "FEATS", "MISC"
    )
}