package org.jgrahn.pattern

sealed interface Result {
    interface Success : Result

    data class Failure(
        val message: String,
        val error: Error,
    ) : Result
}

