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
    NotApplicable(false),
}

const val THREAD_LIMIT = 3

data class ExecutionResult<T>(
    val success: Boolean,
    val failureReasons: Map<StopId, String> = emptyMap(),
)

suspend fun <T, K: PassengerList> routeStops(context: RouteHandlerContext<T, K>): ExecutionResult<T> {
    val boardedPassengerIdsAtom = AtomicReference(context.initialPassengerIdSet)
    val executionStatusAtom = AtomicReference(
        context.route.stops.associateWith { ExecutionStatus.Pending }.toMutableMap()
    )
    val failureReasonsAtom = AtomicReference<Map<StopId, String>>(emptyMap())
    val threadSemaphore = Semaphore(THREAD_LIMIT)
    val stateChanged = Channel<Unit>(Channel.UNLIMITED)

    routeStopsWithState(
        context,
        boardedPassengerIdsAtom,
        executionStatusAtom,
        failureReasonsAtom,
        threadSemaphore,
        stateChanged
    )

    val hasFailures = executionStatusAtom.get().values.any { it == ExecutionStatus.Failed }
    return ExecutionResult(
        success = !hasFailures,
        failureReasons = failureReasonsAtom.get()
    )
}

private suspend fun <T, K: PassengerList> routeStopsWithState(
    context: RouteHandlerContext<T, K>,
    boardedPassengerIdsAtom: AtomicReference<Set<T>>,
    executionStatusAtom: AtomicReference<MutableMap<Stop<T, *>, ExecutionStatus>>,
    failureReasonsAtom: AtomicReference<Map<StopId, String>>,
    threadSemaphore: Semaphore,
    stateChanged: Channel<Unit>,
) {
    suspend fun executeStop(stop: Stop<T, *>) {
        threadSemaphore.acquire()
        try {
            val result = handleStop(stop, context)
            when (result) {
                is StopExecutionResult.FanOut<*, *> -> {
                    val fanOutContexts = result.contexts as List<RouteHandlerContext<T, K>>
                    fanOutContexts.forEach { fanOutContext ->
                        routeStopsWithState(
                            fanOutContext,
                            boardedPassengerIdsAtom,
                            executionStatusAtom,
                            failureReasonsAtom,
                            threadSemaphore,
                            stateChanged
                        )
                    }
                    boardedPassengerIdsAtom.getAndUpdate { it + stop.produces }
                    executionStatusAtom.get()[stop] = ExecutionStatus.Completed
                }
                is StopExecutionResult.Single<*, *> -> {
                    when (result.result) {
                        is Result.Success -> {
                            if (result.result is ActionResult) {
                                handleActionResult(result.result, context.passengerList, context.routeHandler.resultHandler)
                            }
                            boardedPassengerIdsAtom.getAndUpdate { it + stop.produces }
                            executionStatusAtom.get()[stop] = ExecutionStatus.Completed
                        }
                        is Result.Failure -> {
                            executionStatusAtom.get()[stop] = ExecutionStatus.Failed
                            failureReasonsAtom.getAndUpdate { it + (stop.stopId to (result.result).message) }
                        }
                    }
                }
                is StopExecutionResult.Conditional<*, *> -> {
                    val conditionalResult = result.result as ConditionalResult<K>
                    if (conditionalResult.passed) {
                        boardedPassengerIdsAtom.getAndUpdate { it + stop.produces }
                        executionStatusAtom.get()[stop] = ExecutionStatus.Completed
                    } else {
                        executionStatusAtom.get()[stop] = ExecutionStatus.NotApplicable
                        failureReasonsAtom.getAndUpdate { it + (stop.stopId to (conditionalResult.reason.orEmpty())) }
                    }
                }
            }
            stateChanged.send(Unit)
        } finally {
            threadSemaphore.release()
        }
    }

    while (executionStatusAtom.get().values.any { it.isRunnable }) {
        val runnableStops = executionStatusAtom.get()
            .filter { (_, status) -> status.isRunnable }
            .filter { (stop, _) ->
                stop.dependsOn.all { passengerId ->
                    boardedPassengerIdsAtom.get().contains(passengerId)
                }
            }
            .keys

        if (runnableStops.isEmpty()) break

        runnableStops.forEach { stop ->
            executionStatusAtom.get()[stop] = ExecutionStatus.InProgress
            executeStop(stop)
        }

        stateChanged.receive()
    }

    stateChanged.close()
}