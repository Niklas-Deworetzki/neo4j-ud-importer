package se.gu.processor

abstract class Annotations {

    abstract fun getString(attribute: String): String?

    abstract fun getList(attribute: String): List<String>?

    abstract fun getMap(attribute: String): Iterable<Pair<String, String>>?

    abstract fun keys(): Set<String>

    override fun equals(other: Any?): Boolean {
        if (other !is Annotations) return false
        if (this.keys() != other.keys()) return false
        return keys().all { this.getString(it) == other.getString(it) }
    }

    override fun hashCode(): Int = keys().fold(0) { acc, key ->
        acc + key.hashCode() + getString(key).hashCode()
    }

    override fun toString(): String = keys().joinToString(prefix = "{", separator = ",", postfix = "}") { key ->
        "$key=\"${getString(key)}\""
    }
}
