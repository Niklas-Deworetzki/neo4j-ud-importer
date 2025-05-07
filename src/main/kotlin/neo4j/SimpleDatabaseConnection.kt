package se.gu.neo4j

import se.gu.application.Configuration

class SimpleDatabaseConnection(configuration: Configuration) : AbstractDatabaseConnection(configuration) {
    override fun executeIteratedQuery(producer: String, consumer: String) = executeQuery(
        """
            CALL apoc.periodic.iterate(
                "$producer",
                "$consumer",
                {}
             )
         """
    )

    override fun prepareBufferedQuery(query: String, parameter: String): BufferedQuery =
        FixedSizeBufferedQuery(query, parameter, BufferedQuery.DEFAULT_BUFFER_SIZE)

    private inner class FixedSizeBufferedQuery(
        query: String,
        parameterName: String,
        bufferSize: Int
    ) : AbstractFixedSizeBufferedQuery(query, parameterName, bufferSize) {
        override fun forceCommit() {
            executeQuery(
                query,
                associateAsQueryParameter(buffer)
            )
            buffer.clear()
        }
    }
}
