package se.gu.neo4j

abstract class AbstractFixedSizeBufferedQuery(
    protected val query: String,
    protected val parameterName: String,
    protected val bufferSize: Int
) : BufferedQuery {
    protected val buffer = ArrayList<Any?>(bufferSize)

    override fun submitParameter(parameter: Any?) {
        buffer.add(parameter)
        if (buffer.size >= bufferSize) {
            forceCommit()
        }
    }

    override fun submitParameters(parameters: List<Any?>) {
        buffer.addAll(parameters)
        if (buffer.size >= bufferSize) {
            forceCommit()
        }
    }

    // This isn't pretty but reduces ~70% of cost when setting up a query
    //   by eliminating allocation & construction of a map every time.
    private val bufferedParameters = OptimizedSingleParameterMap<Any>()
    // And we expose this trick using a nice functional interface.
    protected fun associateAsQueryParameter(value: Any): Map<String, Any> {
        bufferedParameters.put(value)
        return bufferedParameters
    }

    private inner class OptimizedMutableMapEntry<V>(
        var valueReference: V? = null
    ) : Map.Entry<String, V> {
        override val key: String
            get() = parameterName
        override val value: V
            get() = valueReference!!
    }

    protected inner class OptimizedSingleParameterMap<V> : Map<String, V> {
        private val entry = OptimizedMutableMapEntry<V>()

        fun put(value: V) {
            this.entry.valueReference = value
        }

        override val entries: Set<Map.Entry<String, V>> =
            setOf(entry) // Reference to mutable entry.
        override val keys: Set<String> =
            setOf(entry.key) // Immutable key defined by query.
        override val values: Collection<V>
            get() = setOf(entry.value) // Computed on demand to ensure current value is present.

        override val size: Int
            get() = 1

        override fun containsKey(key: String): Boolean =
            entry.key == key

        override fun containsValue(value: V): Boolean =
            entry.valueReference == value

        override fun get(key: String): V? =
            if (containsKey(key)) entry.valueReference else null

        override fun getOrDefault(key: String, defaultValue: V): V =
            if (containsKey(key)) entry.valueReference!! else defaultValue

        override fun isEmpty(): Boolean =
            false

        override fun equals(other: Any?): Boolean =
            other is Map<*, *> && other.size == 1 && other[entry.key] == entry.valueReference

        override fun hashCode(): Int =
            entry.valueReference.hashCode()

        override fun toString(): String =
            "{${entry.key}=${entry.valueReference}}"
    }
}
