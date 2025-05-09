package corpus.regions

import corpus.prepare.CorpusStatistics
import se.gu.corpus.Encoding.CONLLU_REGION_SENTENCE
import se.gu.corpus.Encoding.DEPENDENCY_RELATION_PROPERTY
import se.gu.corpus.Encoding.DEPENDENCY_RELATION_TYPE
import se.gu.corpus.Encoding.POSITION_PROPERTY
import se.gu.corpus.Encoding.REGION_LEFT_BOUND_PROPERTY
import se.gu.corpus.Encoding.REGION_RIGHT_BOUND_PROPERTY
import se.gu.corpus.Encoding.ROOT_NODE_LABEL
import se.gu.corpus.Encoding.SUCCESSOR_RELATIONSHIP_TYPE
import se.gu.corpus.Encoding.WORD_NODE_LABEL
import se.gu.neo4j.NamingConventions

interface Relationship {
    val unitsOfWork: Long

    val producerQuery: String

    val consumerQuery: String

    class Successor(corpusStatistics: CorpusStatistics) : Relationship {
        private val sentenceLabel = NamingConventions.toNodeLabel(CONLLU_REGION_SENTENCE)

        override val unitsOfWork: Long =
            corpusStatistics.wordsCoveredByRegion(CONLLU_REGION_SENTENCE) -
                    corpusStatistics.occurrencesOfRegion(CONLLU_REGION_SENTENCE)

        override val producerQuery: String = """
            MATCH (region:`$sentenceLabel`)
            UNWIND range(region.`$REGION_LEFT_BOUND_PROPERTY`, region.`$REGION_RIGHT_BOUND_PROPERTY` - 1) AS position
            RETURN position
        """

        override val consumerQuery: String = """
            MATCH (src:`$WORD_NODE_LABEL` {`$POSITION_PROPERTY`: position})
            MATCH (dst:`$WORD_NODE_LABEL` {`$POSITION_PROPERTY`: position + 1})
            CREATE (src)-[:`$SUCCESSOR_RELATIONSHIP_TYPE`]->(dst)
        """
    }

    class Root(corpusStatistics: CorpusStatistics) : Relationship {
        private val sentenceLabel = NamingConventions.toNodeLabel(CONLLU_REGION_SENTENCE)

        override val unitsOfWork: Long =
            corpusStatistics.wordsCoveredByRegion(CONLLU_REGION_SENTENCE)

        override val producerQuery: String = """
            MATCH (region:`$sentenceLabel`)
            MATCH (word:`$ROOT_NODE_LABEL`:`$WORD_NODE_LABEL`)
            WHERE word.position >= region.`$REGION_LEFT_BOUND_PROPERTY`
            AND word.position <= region.`$REGION_RIGHT_BOUND_PROPERTY`
            RETURN word, region
        """

        override val consumerQuery: String = """
            CREATE (word)<-[:`$DEPENDENCY_RELATION_TYPE` {`$DEPENDENCY_RELATION_PROPERTY`: 'root'}]-(region)
        """
    }

    class ParentRegion(corpusStatistics: CorpusStatistics, region: String, parent: String) :
        Relationship {
        private val regionNodeLabel = NamingConventions.toNodeLabel(region)
        private val parentNodeLabel = NamingConventions.toNodeLabel(parent)
        private val relationshipType = NamingConventions.toRelationshipType(parent)

        override val unitsOfWork: Long =
            corpusStatistics.occurrencesOfRegion(region)

        override val producerQuery: String = """
            MATCH (parent:`$parentNodeLabel`)
            MATCH (region:`$regionNodeLabel`)
            WHERE parent.`$REGION_LEFT_BOUND_PROPERTY` <= region.`$REGION_LEFT_BOUND_PROPERTY` AND
            region.`$REGION_RIGHT_BOUND_PROPERTY` >= parent.`$REGION_RIGHT_BOUND_PROPERTY`
            RETURN region, parent
        """

        override val consumerQuery: String = """
            CREATE (region)-[:`$relationshipType`]->(parent)
        """
    }

    class MembershipToRegion(corpusStatistics: CorpusStatistics, region: String) : Relationship {
        private val regionNodeLabel = NamingConventions.toNodeLabel(region)
        private val relationshipType = NamingConventions.toRelationshipType(region)

        override val unitsOfWork: Long =
            corpusStatistics.wordsCoveredByRegion(region)

        override val producerQuery: String = """
            EXPLAIN MATCH (region:`$regionNodeLabel`)
            UNWIND range(region.`$REGION_LEFT_BOUND_PROPERTY`, region.`$REGION_RIGHT_BOUND_PROPERTY`) AS position
            MATCH (word:`$WORD_NODE_LABEL` {`$POSITION_PROPERTY`: position})
            RETURN region, position
        """
        override val consumerQuery: String = """
            CREATE (word)<-[:`$relationshipType`]-(region)
        """
    }
}
