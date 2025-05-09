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

}
