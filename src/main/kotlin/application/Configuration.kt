package se.gu.application

import picocli.CommandLine
import se.gu.neo4j.DatabaseConnection
import se.gu.neo4j.SimpleDatabaseConnection
import java.io.File
import java.lang.reflect.Field

@CommandLine.Command(
    name = "neo4jud",
    versionProvider = VersionProvider::class,
    abbreviateSynopsis = true,
    usageHelpAutoWidth = true,
    description = [
        "Import UD treebanks into a Neo4j database."
    ],
    parameterListHeading = "%nParameters:%n",
    footerHeading = "%n",
    footer = [
        "Copyright (C) 2025 Niklas Deworetzki"
    ],
)
class Configuration {
    @CommandLine.Parameters(
        paramLabel = "CORPUS-FILES",
        description = [
            "UD treebank files to index.",
            "If a directory is provided, all .conllu files in the directory and its subdirectories will be indexed."
        ]
    )
    var corpusFiles: List<File> = mutableListOf()

    @CommandLine.Option(
        names = ["--help"],
        description = ["Print this message and exit."]
    )
    var isHelpRequested: Boolean = false

    @CommandLine.Option(
        names = ["--version"],
        description = ["Print the version number."]
    )
    var isVersionRequested: Boolean = false

    @CommandLine.ArgGroup(
        validate = false,
        heading = "%nDatabase options:%n"
    )
    var databaseOptions: DatabaseOptions = DatabaseOptions()

    @CommandLine.ArgGroup(
        exclusive = true,
        heading = "%nDatabase options:%n"
    )
    var annotationOptions: AnnotationOptions = AnnotationOptions()

    @CommandLine.ArgGroup(
        validate = false,
        heading = "%nDatabase options:%n"
    )
    var encodingOptions: EncodingOptions = EncodingOptions()

    class DatabaseOptions {
        @CommandLine.Option(
            names = ["-h", "--host"],
            required = false,
            description = ["Host to connect to. Defaults to 'localhost'."]
        )
        var databaseHost: String = "localhost"

        @CommandLine.Option(
            names = ["-p", "--port"],
            required = false,
            description = ["Port to connect to. Defaults to '7687'."]
        )
        var databasePort: Int = 7687

        @CommandLine.Option(
            names = ["-d", "--database"],
            required = false,
            description = ["Name of the database. Defaults to 'neo4j'."]
        )
        var databaseName: String = "neo4j"
    }

    class AnnotationOptions {
        @CommandLine.Option(
            names = ["--as-properties"],
            required = false,
            description = ["Annotations will be encoded as properties on nodes."]
        )
        var asProperties: Boolean = false

        @CommandLine.Option(
            names = ["--as-nodes"],
            required = false,
            description = ["Annotations will be encoded as additional nodes."]
        )
        var asNodes: Boolean = false
    }

    class EncodingOptions {
        @CommandLine.Option(
            names = ["--encode-documents"],
            required = false,
            negatable = true,
            description = ["Choose to encode documents as additional nodes. Enabled by default."]
        )
        var encodeDocuments: Boolean = true

        @CommandLine.Option(
            names = ["--encode-paragraphs"],
            required = false,
            negatable = true,
            description = ["Choose to encode paragraphs as additional nodes. Enabled by default."]
        )
        var encodeParagraphs: Boolean = true

        @CommandLine.Option(
            names = ["--encode-mwt"],
            required = false,
            negatable = true,
            description = ["Choose to encode multiword tokens as additional nodes. Enabled by default."]
        )
        var encodeMwts: Boolean = true
    }


    val databaseConnection: DatabaseConnection by lazy { SimpleDatabaseConnection(this) }

    fun allCorpusFiles(): List<File> = corpusFiles.flatMap {
        if (it.isFile) listOf(it) else listFilesRecursively(it)
    }

    private fun listFilesRecursively(root: File): List<File> = when {
        root.isDirectory ->
            root.listFiles()!!.flatMap { listFilesRecursively(it) }

        root.isFile && root.name.endsWith(".conllu") ->
            listOf(root)

        else ->
            emptyList()
    }

    val databaseUrl: String
        get() = "neo4j://${databaseOptions.databaseHost}:${databaseOptions.databasePort}"

    override fun toString(): String {
        val fields = javaClass.declaredFields
            .filter { includeInToString(it) }
            .sortedBy { it.name }
        return fields.joinToString(prefix = "Configuration{", separator = ",", postfix = "}") {
            it.isAccessible = true
            "${it.name}=${it.get(this)}"
        }
    }

    private companion object {
        private val INCLUDED_ANNOTATIONS = setOf(
            CommandLine.Option::class.java,
            CommandLine.ArgGroup::class.java
        )

        fun includeInToString(field: Field): Boolean =
            INCLUDED_ANNOTATIONS.any { field.isAnnotationPresent(it) }
    }
}