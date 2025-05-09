package se.gu.corpus

import se.gu.application.Configuration

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

    const val WORD_NODE_LABEL = "Word"
    const val POSITION_PROPERTY = "position"
    const val INVENTORY_PROPERTY = "value"

    const val DEPENDENCY_RELATION_TYPE = "DEPREL"
    const val DEPENDENCY_RELATION_PROPERTY = "deprel"
    const val ROOT_NODE_LABEL = "Root"

    const val REGION_LEFT_BOUND_PROPERTY = "begin"
    const val REGION_RIGHT_BOUND_PROPERTY = "end"

    const val SUCCESSOR_RELATIONSHIP_TYPE = "SUCCESSOR"

    const val CONLLU_REGION_MWT = "MultiWordToken"
    const val CONLLU_REGION_SENTENCE = "sentence"
    const val CONLLU_REGION_PARAGRAPH = "paragraph"
    const val CONLLU_REGION_DOCUMENT = "document"

    fun includedRegions(configuration: Configuration): Set<String> {
        val regions = mutableSetOf(CONLLU_REGION_SENTENCE)
        if (configuration.encodingOptions.encodeMwts) {
            regions.add(CONLLU_REGION_MWT)
        }
        if (configuration.encodingOptions.encodeParagraphs) {
            regions.add(CONLLU_REGION_PARAGRAPH)
        }
        if (configuration.encodingOptions.encodeDocuments) {
            regions.add(CONLLU_REGION_DOCUMENT)
        }
        return regions
    }
}
