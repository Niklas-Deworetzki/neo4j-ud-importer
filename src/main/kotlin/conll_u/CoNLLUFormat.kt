package se.gu.conll_u

object CoNLLUFormat {
    const val EMPTY_VALUE = "_"
    const val LIST_ITEM_SEPARATOR = '|'
    const val KEY_VALUE_SEPARATOR = '='
    const val MULTIPLE_VALUES_SEPARATOR = ','
    const val ENHANCED_DEPENDENCY_SEPARATOR = ':'

    val COLUMNS = listOf(
        "ID",
        "FORM",
        "LEMMA",
        "UPOS",
        "XPOS",
        "FEATS",
        "HEAD",
        "DEPREL",
        "DEPS",
        "MISC"
    )

    fun parseList(string: String): List<String> =
        if (string == EMPTY_VALUE) emptyList()
        else string.split(LIST_ITEM_SEPARATOR)

    fun parseMap(string: String): Iterable<Pair<String, String>> {
        for (separator in setOf(KEY_VALUE_SEPARATOR, ENHANCED_DEPENDENCY_SEPARATOR)) {
            try {
                return parseAssociativeList(string, separator, mutableListOf()) { key, values ->
                    add(key to values)
                }
            } catch (_: StringIndexOutOfBoundsException) {
                // try again with another separator.
            }
        }
        return emptyList()
    }

    fun parseEnhancedDependencyRelation(string: String): Map<String, String> =
        parseAssociativeList(string, ENHANCED_DEPENDENCY_SEPARATOR, mutableMapOf()) { key, value ->
            put(key, value)
        }


    private inline fun <C> parseAssociativeList(
        value: String,
        separator: Char,
        destination: C,
        addToDestination: C.(String, String) -> Unit
    ): C {
        val entries = parseList(value)
        for (entry in entries) {
            val separatorIndex = entry.indexOf(separator)
            val keyString = entry.substring(0, separatorIndex)
            val valueString = entry.substring(separatorIndex + 1)
            destination.addToDestination(keyString, valueString)
        }
        return destination
    }
}
