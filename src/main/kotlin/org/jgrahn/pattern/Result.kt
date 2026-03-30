package org.jgrahn.pattern

sealed interface Result {
    interface Success : Result

    data class Failure(
        val message: String,
        val error: Error,
    ) : Result
}

data class ConditionalResult<K: PassengerList>(
    val passed: Boolean,
    val reason: String? = null,
    val passengerList: K? = null,
)
