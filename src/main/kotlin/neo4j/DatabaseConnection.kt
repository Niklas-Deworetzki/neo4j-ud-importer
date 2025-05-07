package se.gu.neo4j

import org.neo4j.driver.EagerResult
import java.io.Closeable

interface DatabaseConnection : Closeable {

    fun executeQueryWithResult(query: String, params: Map<String, Any?> = emptyMap()): EagerResult

    fun executeIteratedQuery(
        producer: String,
        consumer: String
    )

    fun prepareBufferedQuery(
        query: String,
        parameter: String
    ): BufferedQuery

    fun addConstraint(constraint: Constraint)

    fun addIndex(index: Index)
}
