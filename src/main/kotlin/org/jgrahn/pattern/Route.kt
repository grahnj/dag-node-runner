package org.jgrahn.pattern

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
                    coroutineScope {
                        fanOutContexts.forEach { fanOutContext ->
                            launch {
                                // completely fresh state for this branch
                                val branchBoardedPassengerIdsAtom =
                                    AtomicReference(boardedPassengerIdsAtom.get().toSet())

                                branchBoardedPassengerIdsAtom.getAndUpdate { it + stop.produces }

                                val branchExecutionStatusAtom =
                                    AtomicReference(executionStatusAtom.get().toMutableMap())

                                branchExecutionStatusAtom.get()[stop] = ExecutionStatus.Completed

                                val branchFailureReasonsAtom =
                                    AtomicReference<Map<StopId, String>>(emptyMap())

                                val branchSemaphore = Semaphore(THREAD_LIMIT)
                                val branchStateChanged = Channel<Unit>(Channel.UNLIMITED)

                                routeStopsWithState(
                                    fanOutContext,
                                    branchBoardedPassengerIdsAtom,
                                    branchExecutionStatusAtom,
                                    branchFailureReasonsAtom,
                                    branchSemaphore,
                                    branchStateChanged,
                                )

//                                stateChanged.close()
                            }
                        }
                    }
                }

                is StopExecutionResult.Single<*, *> -> {
                    when (val singleResult = result.result) {
                        is Result.Success -> {
                            when (singleResult) {
                                is ActionResult -> {
                                    handleActionResult(singleResult, context.passengerList, context.routeHandler.resultHandler)
                                    boardedPassengerIdsAtom.getAndUpdate { it + stop.produces }
                                    executionStatusAtom.get()[stop] = ExecutionStatus.Completed
                                }
                            }
                        }
                        is Result.Failure -> {
                            executionStatusAtom.get()[stop] = ExecutionStatus.Failed
                            failureReasonsAtom.getAndUpdate { it + (stop.stopId to singleResult.message) }
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
            if (!stateChanged.isClosedForSend) {
                stateChanged.send(Unit)
            }
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

        if (!stateChanged.isClosedForReceive) {
            stateChanged.receive()
        }
    }

    if (!stateChanged.isClosedForSend) {
        stateChanged.close()
    }
}