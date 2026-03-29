package org.jgrahn.pattern

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore
import java.util.concurrent.atomic.AtomicReference

enum class ExecutionStatus(
    val isRunnable: Boolean,
) {
    Pending(true),
    InProgress(false),
    Failed(false),
    Completed(false),
}

data class RouteHandlerContext<T, K: PassengerList>(
    val initialPassengerIdSet: Set<T>,
    val passengerList: K,
    val stops: Set<Stop<T>>,
    val hooks: InteractionHooks,
    val routeHandler: RouteHandler<T, K>,
)

const val THREAD_LIMIT = 3

suspend fun <T, K: PassengerList> routeStops(context: RouteHandlerContext<T, K>) {
    val stops = context.stops
    val handler = context.routeHandler
    val boardedPassengerIdsAtom = AtomicReference(context.initialPassengerIdSet)
    val executionStatusAtom = AtomicReference(
        stops.associateWith { ExecutionStatus.Pending }
            .toMutableMap()
    )
    val threadSemaphore = Semaphore(THREAD_LIMIT)
    val stateChanged = Channel<Unit>(Channel.UNLIMITED)

    // TODO: This is wrong, we shouldn't change the state on the same map that we're iterating over
    val canExecute = executionStatusAtom
        .get()
        .filter { (_, v) -> v.isRunnable }
        .filter { (k, _) ->
            k.dependsOn.all { passengerId ->
                boardedPassengerIdsAtom
                    .get()
                    .contains(passengerId)
            }
        }
        .forEach { (k, v) ->
            // TODO: spin up a thread and a channel
            executionStatusAtom.get()[k] = ExecutionStatus.InProgress
            // this is dumb to do this right here, the control flow should be ours not the callers
            // this is going to get run async
            when (val result = handleStop(k, context)) {
                is Result.Success -> {
                    when (result) {
                        is ActionResult -> {
                            handleActionResult(result, context.passengerList, context.routeHandler.resultHandler)
                            executionStatusAtom.get()[k] = ExecutionStatus.Completed
                        }
                        else -> {
                            executionStatusAtom.get()[k] = ExecutionStatus.Failed
                            throw NotImplementedError("This route should never happen: ${k.stopId} : $result")
                        }
                    }
                }
                is Result.Failure -> {
                    // TODO: Handle this failure
                    executionStatusAtom.get()[k] = ExecutionStatus.Failed
                }
            }
            // put the result into the passenger list
            // update the stop status to be completed or failed depending on the result
            // (our internal executor should handle the stop status stuff)
        }
}