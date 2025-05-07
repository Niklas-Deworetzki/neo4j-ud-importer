package se.gu.neo4j

sealed interface Constraint {

    fun asQuery(): String


    data class Unique(private val label: String, private val property: String) : Constraint {
        override fun asQuery(): String = """
            CREATE CONSTRAINT `${label}_${property}_unique`
            IF NOT EXISTS
            FOR (node:`$label`)
            REQUIRE node.`$property` IS UNIQUE
        """
    }
}
