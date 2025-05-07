package se.gu.corpus.annotations

import se.gu.neo4j.Constraint
import se.gu.neo4j.DatabaseConnection
import se.gu.neo4j.NamingConventions
import se.gu.neo4j.NamingConventions.INVENTORY_PROPERTY
import java.io.Closeable

class InventoryBuilder(private val connection: DatabaseConnection) : Closeable {
    private val inventories = HashMap<String, Inventory>()

    fun addToInventory(inventory: String, value: Any) {
        val normalizedName = NamingConventions.toNodeLabel(inventory)
        inventories.getOrPut(normalizedName) { Inventory(normalizedName) }
            .add(value)
    }

    override fun close() {
        inventories.values.forEach { it.bufferedQuery.forceCommit() }
    }

    private inner class Inventory(label: String) {
        val bufferedQuery = connection.prepareBufferedQuery(
            """
                UNWIND ${'$'}batch AS value
                CREATE (inventory: `$label`)
                SET inventory.`${INVENTORY_PROPERTY}` = value
            """,
            "batch"
        )

        init {
            val constraint = Constraint.Unique(label, INVENTORY_PROPERTY)
            connection.addConstraint(constraint)
        }

        val knownValues = HashSet<Any>()

        fun add(value: Any) {
            if (knownValues.add(value)) {
                bufferedQuery.submitParameter(value)
            }
        }
    }
}
