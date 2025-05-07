package se.gu

import picocli.CommandLine
import se.gu.application.Configuration
import kotlin.system.exitProcess

object UDImporter {

    @JvmStatic
    fun main(args: Array<String>) {
        val configuration = Configuration()
        val cmd = CommandLine(configuration)
        try {
            cmd.parseArgs(*args)

            when {
                args.isEmpty() -> {
                    cmd.usage(System.out)
                    exitProcess(1)
                }

                configuration.isHelpRequested ->
                    cmd.usage(System.out)

                configuration.isVersionRequested ->
                    cmd.printVersionHelp(System.out)

                else ->
                    CorpusIndexer().use { indexer ->
                        indexer.importCorpusToDatabase(configuration)
                    }
            }
            exitProcess(0)
        } catch (parseFailure: CommandLine.ParameterException) {
            System.err.println(parseFailure.message)
            if (!CommandLine.UnmatchedArgumentException.printSuggestions(parseFailure, System.err)) {
                cmd.usage(System.err)
            }
            exitProcess(1)
        } catch (exception: Exception) {
            System.err.println("Importing the corpus failed with the following error:")
            System.err.println(exception.message)
            exitProcess(2)
        }
    }
}
