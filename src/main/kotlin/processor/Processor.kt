package se.gu.processor

import se.gu.application.Configuration

abstract class Processor(open val configuration: Configuration) {

    open fun <T> processTask(task: Task<T>): ProcessingTask<T> =
        ProcessingTask(task) {}

    fun <T> processTask(task: CorpusTask<T>): ProcessingTask<T> =
        ProcessingTask(task) {
            applyToCorpus(task)
        }

    protected abstract fun applyToCorpus(corpusTask: CorpusTask<*>)


    inner class ProcessingTask<T>(private val task: Task<T>, private val execution: (Task<T>) -> Unit) : Runnable {
        init {
            task.configuration = configuration
        }
        val progress: Progress?
            get() = task.progress

        val value by lazy { forceRunTask() }

        override fun run() {
            value // Force evaluation of lazy variable.
        }

        private fun forceRunTask(): T {
            try {
                task.setup()
                execution.invoke(task)
                task.performComputation()
            } finally {
                task.teardown()
            }
            return task.getValue()
        }
    }
}
