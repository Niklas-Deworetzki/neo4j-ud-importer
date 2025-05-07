package corpus.prepare

import se.gu.neo4j.Constraint
import se.gu.neo4j.NamingConventions.POSITION_PROPERTY
import se.gu.neo4j.NamingConventions.WORD_NODE_LABEL
import se.gu.processor.Task

class WordNodeCreationTask(private val statistics: CorpusStatistics) : Task<Unit>() {
    override fun getValue() = Unit

    override fun performComputation() {
        configuration.databaseConnection.addConstraint(Constraint.Unique(WORD_NODE_LABEL, POSITION_PROPERTY))

        configuration.databaseConnection.executeIteratedQuery(
            "UNWIND range(0, ${statistics.corpusSize - 1}) AS position RETURN position",
            "CREATE (:`${WORD_NODE_LABEL}` {`${POSITION_PROPERTY}`: position})"
        )
    }
}
