package corpus.prepare

class MutableCounter : Counter {
    private var occurrences: Long = 0L
    private val attributeOccurrences = mutableMapOf<String, Long>()

    fun increment() =
        occurrences++

    fun incrementAttribute(attribute: String) =
        attributeOccurrences.compute(attribute) { _, counter ->
            counter?.inc() ?: 1
        }

    override fun occurrences(): Long =
        occurrences

    override fun attributeOccurrences(attribute: String): Long =
        attributeOccurrences[attribute] ?: 0
}
