package corpus.prepare

interface Counter {

    fun occurrences(): Long

    fun attributeOccurrences(attribute: String): Long

}