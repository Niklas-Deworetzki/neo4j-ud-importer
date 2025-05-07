package se.gu.neo4j

import org.neo4j.driver.EagerResult
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.QueryConfig
import se.gu.application.Configuration

abstract class AbstractDatabaseConnection(configuration: Configuration) :
    DatabaseConnection {
    private val driver = GraphDatabase.driver(configuration.databaseUrl)
    private val queryConfig: QueryConfig = QueryConfig.builder()
        .withDatabase(configuration.databaseOptions.databaseName)
        .build()

    init {
        driver.verifyConnectivity()
    }

    override fun executeQueryWithResult(query: String, params: Map<String, Any?>): EagerResult =
        driver.executableQuery(query)
            .withConfig(queryConfig)
            .withParameters(params)
            .execute()

    fun executeQuery(query: String, params: Map<String, Any?> = emptyMap()) {
        driver.executableQuery(query)
            .withConfig(queryConfig)
            .withParameters(params)
            .execute()
    }

    override fun addConstraint(constraint: Constraint) =
        executeQuery(constraint.asQuery())

    override fun addIndex(index: Index) =
        executeQuery(index.asQuery())

    override fun close() =
        driver.close()
}
