package corpus.regions

import se.gu.corpus.Encoding.REGION_LEFT_BOUND_PROPERTY
import se.gu.corpus.Encoding.REGION_RIGHT_BOUND_PROPERTY
import se.gu.neo4j.Constraint
import se.gu.neo4j.DatabaseConnection
import se.gu.neo4j.NamingConventions
import java.io.Closeable

class RegionBuilder(private val connection: DatabaseConnection) : Closeable {
    private val regions = HashMap<String, Region>()

    private fun getRegion(region: String): Region {
        val normalizedName = NamingConventions.toNodeLabel(region)
        return regions.getOrPut(normalizedName) { Region(normalizedName) }
    }

    fun enter(region: String, position: Long) {
        getRegion(region).enter(position)
    }

    fun exit(region: String, position: Long) {
        getRegion(region).exit(position)
    }

    override fun close() {
        regions.values.forEach { it.bufferedQuery.forceCommit() }
    }

    private inner class Region(label: String) {
        val bufferedQuery = connection.prepareBufferedQuery(
            """
                 UNWIND ${'$'}batch AS structure
                 WITH structure
                 CREATE (node:`${label}` {
                    `$REGION_LEFT_BOUND_PROPERTY`: structure.lpos,
                    `$REGION_RIGHT_BOUND_PROPERTY`: structure.rpos
                 })
            """,
            "batch"
        )

        init {
            val constraints = listOf(
                Constraint.Unique(label, REGION_LEFT_BOUND_PROPERTY),
                Constraint.Unique(label, REGION_RIGHT_BOUND_PROPERTY)
            )
            constraints.forEach(connection::addConstraint)
        }

        private var openedPosition: Long = 0

        fun enter(atPosition: Long) {
            this.openedPosition = atPosition
        }

        fun exit(atPosition: Long) {
            val queryParameter = mapOf(
                "lpos" to openedPosition,
                "rpos" to atPosition
            )
            bufferedQuery.submitParameter(queryParameter)
        }
    }
}
