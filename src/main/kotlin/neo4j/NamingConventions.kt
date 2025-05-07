package se.gu.neo4j

import com.github.sttk.stringcase.StringCase

object NamingConventions {

    private inline fun convertOrFallback(value: String, converter: (String) -> String): String {
        val result = converter(value)
        if (result.isBlank()) return value
        return result
    }

    fun toNodeLabel(name: String): String =
        convertOrFallback(name, StringCase::pascalCase)

    fun toPropertyName(name: String): String =
        convertOrFallback(name, StringCase::camelCase)

    fun toRelationshipType(name: String): String =
        convertOrFallback(name, StringCase::macroCase)

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
}
