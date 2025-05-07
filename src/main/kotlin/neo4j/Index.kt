package se.gu.neo4j

interface Index {

    fun asQuery(): String

    data class OnRelationshipProperty(private val type: String, private val property: String) : Index {
        override fun asQuery(): String = """
            CREATE INDEX `relation_${type}_index`
            IF NOT EXISTS
            FOR ()-[rel:`${type}`]-()
            ON rel.`${property}`
        """
    }
}
