package se.gu.neo4j

interface BufferedQuery {
    /**
     * Add a single parameter to the query.
     *
     * Commits the buffer if added parameter matches or exceeds buffer size.
     */
    fun submitParameter(parameter: Any?)

    /**
     * Add a list of parameters to the query.
     *
     * Commits the buffer if added parameters match or exceed buffer size.
     */
    fun submitParameters(parameters: List<Any?>)

    /**
     * Commits the buffer, irrespective of buffer size or contents.
     */
    fun forceCommit()

    companion object {
        const val DEFAULT_BUFFER_SIZE = 10000
    }
}
