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
        get() = relationships.sumOf { it.unitsOfWork }
    private var completedUnitsOfWork: Long = 0L

    override val progress: Progress
        get() = Progress(completedUnitsOfWork, totalUnitsOfWork)

    override fun setup() {
        val tasks = mutableListOf<Relationship>()
        tasks.add(Relationship.Successor(statistics))
        tasks.add(Relationship.Root(statistics))

        if (configuration.encodingOptions.encodeParagraphs) {
            tasks.add(Relationship.ParentRegion(statistics, CONLLU_REGION_SENTENCE, CONLLU_REGION_PARAGRAPH))
        }
        if (configuration.encodingOptions.encodeDocuments) {
            if (configuration.encodingOptions.encodeParagraphs) {
                tasks.add(Relationship.ParentRegion(statistics, CONLLU_REGION_PARAGRAPH, CONLLU_REGION_DOCUMENT))
            } else {
                tasks.add(Relationship.ParentRegion(statistics, CONLLU_REGION_SENTENCE, CONLLU_REGION_DOCUMENT))
            }
        }
        if (configuration.encodingOptions.encodeMwts) {
            tasks.add(Relationship.MembershipToRegion(statistics, CONLLU_REGION_MWT))
        }

        this.relationships.addAll(tasks)
    }

    override fun performComputation() {
        for (relationship in relationships) {
            configuration.databaseConnection.executeIteratedQuery(
                relationship.producerQuery,
                relationship.consumerQuery
            )
        }
    }

    override fun getValue() = Unit
}
