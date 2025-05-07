package se.gu

import corpus.prepare.ImportPreparationTask
import corpus.prepare.WordNodeCreationTask
import me.tongfei.progressbar.ProgressBar
import org.fusesource.jansi.AnsiConsole
import se.gu.ExtendedSyntax.ifPresent
import se.gu.application.Configuration
import se.gu.conll_u.CoNLLUProcessor
import se.gu.corpus.annotations.AnnotationsImporterTask
import se.gu.corpus.annotations.AnnotationNodeImporterTask
import corpus.regions.RegionImportTask
import corpus.regions.AdditionalRelationshipsImporterTask
import se.gu.corpus.tree.TreeImportTask
import se.gu.processor.Processor
import se.gu.processor.Progress
import se.gu.processor.SimpleProgressBarRenderer
import java.io.Closeable
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeoutException

class CorpusIndexer : Closeable {

    private val executor = Executors.newSingleThreadExecutor {
        Thread(it).apply { name = "corpus-importer" }
    }

    private fun print(message: String, vararg args: Any?) =
        System.err.printf(message + System.lineSeparator(), *args)

    private fun checkDatabaseConnection(configuration: Configuration) {
        try {
            configuration.databaseConnection
        } catch (exception: Exception) {
            throw IllegalArgumentException(
                "Database connection could not be established: ${exception.message}",
                exception
            )
        }
    }

    fun importCorpusToDatabase(configuration: Configuration) {
        checkDatabaseConnection(configuration)
        val processor = CoNLLUProcessor(configuration)

        val startTime = System.currentTimeMillis()
        print("Preparing corpus...")
        val statistics = processor
            .processTask(ImportPreparationTask())
            .value
        processor
            .processTask(WordNodeCreationTask(statistics))
            .run()

        processor.processTask(RegionImportTask(statistics))
            .runWithProgressInformation("Scanning corpus for regions to import...")


        if (!configuration.annotationOptions.asProperties) {
            processor.processTask(AnnotationNodeImporterTask(statistics))
                .runWithProgressInformation("Scanning corpus to build inventory of annotations...")
        }

        processor.processTask(AnnotationsImporterTask(statistics))
            .runWithProgressInformation("Importing annotations...")

        processor.processTask(TreeImportTask(statistics))
            .runWithProgressInformation("Importing relationships between words...")

        processor.processTask(AdditionalRelationshipsImporterTask(statistics))
            .runWithProgressInformation("Adding metadata to regions...")

        print("\nFinished in %.1fs", (System.currentTimeMillis() - startTime) / 1000.0)
    }

    override fun close() {
        executor.shutdown()
    }

    private fun getTerminalWidth(): Int {
        val terminalWidth = AnsiConsole.getTerminalWidth()
        return if (terminalWidth == 0) 80
        else terminalWidth
    }

    private fun <T> Processor.ProcessingTask<T>.runWithProgressInformation(task: String): T {
        print(task)

        val progressBarBuilder = ProgressBar.builder()
            .setRenderer(SimpleProgressBarRenderer)
            .setMaxRenderedLength(getTerminalWidth())
        progress.ifPresent {
            progressBarBuilder.setInitialMax(it.total)
        }

        progressBarBuilder.build().use {
            val thread = executor.submit(this)

            val waitingTime = Duration.ofSeconds(1)
            while (!thread.waitForCompletion(waitingTime)) {
                it.applyProgress(progress)
            }
            it.applyProgress(progress)
        }
        return this.value
    }

    private fun ProgressBar.applyProgress(currentProgress: Progress?) =
        currentProgress.ifPresent { progress ->
            this.stepTo(progress.processed)
            this.maxHint(progress.total)
        }

    private fun Future<*>.waitForCompletion(duration: Duration): Boolean {
        try {
            Thread.sleep(duration.toMillis())
            return this.state() != Future.State.RUNNING
        } catch (_: TimeoutException) {
            return false
        }
    }
}
