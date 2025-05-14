package corpus.regions

import corpus.prepare.CorpusStatistics
import se.gu.corpus.Encoding.CONLLU_REGION_DOCUMENT
import se.gu.corpus.Encoding.CONLLU_REGION_MWT
import se.gu.corpus.Encoding.CONLLU_REGION_PARAGRAPH
import se.gu.corpus.Encoding.CONLLU_REGION_SENTENCE
import se.gu.processor.Progress
import se.gu.processor.Task

class AdditionalRelationshipsImporterTask(private val statistics: CorpusStatistics) : Task<Unit>() {

    private val relationships = mutableListOf<Relationship>()

    private val totalUnitsOfWork: Long
        get() = 1 + relationships.sumOf { it.unitsOfWork }
    private var completedUnitsOfWork: Long = 0L

    override val progress: Progress
        get() = Progress(completedUnitsOfWork, totalUnitsOfWork)

    override fun setup() {
        val tasks = mutableListOf<Relationship>()
        tasks.add(Relationship.Successor(statistics))
        tasks.add(Relationship.Root(statistics))
        tasks.add(Relationship.MembershipToRegion(statistics, CONLLU_REGION_SENTENCE))

        if (configuration.encodingOptions.encodeParagraphs) {
            tasks.add(
                Relationship.MembershipToRegion(statistics, CONLLU_REGION_PARAGRAPH)
            )
            tasks.add(
                Relationship.ParentRegionViaMembership(
                    statistics,
                    CONLLU_REGION_SENTENCE,
                    CONLLU_REGION_PARAGRAPH
                )
            )
        }
        if (configuration.encodingOptions.encodeDocuments) {
            tasks.add(
                Relationship.MembershipToRegion(statistics, CONLLU_REGION_DOCUMENT)
            )

            val lowerLevel =
                if (configuration.encodingOptions.encodeParagraphs) CONLLU_REGION_PARAGRAPH
                else CONLLU_REGION_SENTENCE
            tasks.add(
                Relationship.ParentRegionViaMembership(
                    statistics,
                    lowerLevel,
                    CONLLU_REGION_DOCUMENT
                )
            )
        }
        if (configuration.encodingOptions.encodeMwts) {
            tasks.add(Relationship.MembershipToRegion(statistics, CONLLU_REGION_MWT))
        }

        this.relationships.addAll(tasks)
    }

    override fun performComputation() {
        completedUnitsOfWork += 1 // Update progress bar, even if it takes longer.
        for (relationship in relationships) {
            configuration.databaseConnection.executeIteratedQuery(
                relationship.producerQuery,
                relationship.consumerQuery
            )
            completedUnitsOfWork += relationship.unitsOfWork
        }
    }

    override fun getValue() = Unit
}
