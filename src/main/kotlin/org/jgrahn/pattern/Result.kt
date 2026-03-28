package org.jgrahn.pattern

sealed interface Result<out T> {
    open class Success<T>(
        val data: T
    ) : Result<T>

    data class Failure(
        val message: String,
        val error: Error,
    ) : Result<Nothing>
}

