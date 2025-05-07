package se.gu.processor

import se.gu.application.Configuration

abstract class Task<T> {
    lateinit var configuration: Configuration

    open fun setup() {}

    open fun performComputation() {}

    open fun teardown() {}

    abstract fun getValue(): T

    open val progress: Progress?
        get() = null
}
