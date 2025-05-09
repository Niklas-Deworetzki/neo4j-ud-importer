package se.gu.corpus.tree

import corpus.prepare.CorpusStatistics
import se.gu.corpus.Encoding.CONLLU_REGION_SENTENCE
import se.gu.corpus.Encoding.DEPENDENCY_RELATION_PROPERTY
import se.gu.corpus.Encoding.DEPENDENCY_RELATION_TYPE
import se.gu.neo4j.Index
import se.gu.processor.Annotations
import se.gu.processor.CorpusTask

class TreeImportTask(statistics: CorpusStatistics) : CorpusTask<Unit>(statistics) {
    private lateinit var treeBuilder: DepTreeBuilder

    override fun setup() {
        super.setup()
        this.treeBuilder = DepTreeBuilder(configuration.databaseConnection)
    }

    override fun teardown() {
        configuration.databaseConnection.addIndex(
            Index.OnRelationshipProperty(DEPENDENCY_RELATION_TYPE, DEPENDENCY_RELATION_PROPERTY)
        )
        this.treeBuilder.close()
    }

    override fun processWord(annotations: Annotations) {
        treeBuilder.addWord(annotations)
    }

    override fun enterRegion(region: String, annotations: Annotations) {
        if (region == CONLLU_REGION_SENTENCE) {
            treeBuilder.startNewSentence(corpusPosition)
        }
    }

    override fun exitRegion(region: String) {
        if (region == CONLLU_REGION_SENTENCE) {
            treeBuilder.endSentence()
        }
    }

    override fun getValue() = Unit
}
