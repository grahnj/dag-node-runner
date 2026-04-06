package org.jgrahn.pattern

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import java.util.concurrent.atomic.AtomicReference
import java.util.UUID

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

data class StopExecutionNode<T>(
    val executionId: String = UUID.randomUUID().toString(),
    val stop: Stop<T, *>,
    val stopId: StopId,
    val status: ExecutionStatus,
    val dependencyStopIds: List<StopId> = emptyList(), // Track what this depends on
    val failureReason: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0,
)

data class ExecutionTree<T>(
    val nodes: Map<String, StopExecutionNode<T>> = emptyMap(), // All nodes keyed by executionId
    val executionOrder: List<String> = emptyList(), // The order nodes were executed (by executionId)
) {
    fun getNodesByStatus(status: ExecutionStatus): List<StopExecutionNode<T>> {
        return nodes.values.filter { it.status == status }
    }
    
    fun flatten(): List<StopExecutionNode<T>> {
        return executionOrder.mapNotNull { nodes[it] }
    }
}

data class ExecutionResult<T>(
    val success: Boolean,
    val failureReasons: Map<StopId, String> = emptyMap(),
    val executionTree: ExecutionTree<T>? = null,
)

suspend fun <T, K: PassengerList> routeStops(context: RouteHandlerContext<T, K>): ExecutionResult<T> {
    val boardedPassengerIdsAtom = AtomicReference(context.initialPassengerIdSet)
    val executionStatusAtom = AtomicReference(
        context.route.stops.associateWith { ExecutionStatus.Pending }.toMutableMap()
    )
    val failureReasonsAtom = AtomicReference<Map<StopId, String>>(emptyMap())
    val executionTreeAtom = AtomicReference<ExecutionTree<T>?>(null)
    val threadSemaphore = Semaphore(THREAD_LIMIT)
    val stateChanged = Channel<Unit>(Channel.UNLIMITED)
    val executionOrderAtom = AtomicReference<MutableList<String>>(mutableListOf())
    val executionNodesAtom = AtomicReference<MutableMap<String, StopExecutionNode<T>>>(mutableMapOf())

    routeStopsWithState(
        context,
        boardedPassengerIdsAtom,
        executionStatusAtom,
        failureReasonsAtom,
        executionTreeAtom,
        threadSemaphore,
        stateChanged,
        executionNodesAtom,
        executionOrderAtom,
    )

    val hasFailures = executionStatusAtom.get().values.any { it == ExecutionStatus.Failed }
    return ExecutionResult(
        success = !hasFailures,
        failureReasons = failureReasonsAtom.get(),
        executionTree = executionTreeAtom.get()
    )
}

private suspend fun <T, K: PassengerList> routeStopsWithState(
    context: RouteHandlerContext<T, K>,
    boardedPassengerIdsAtom: AtomicReference<Set<T>>,
    executionStatusAtom: AtomicReference<MutableMap<Stop<T, *>, ExecutionStatus>>,
    failureReasonsAtom: AtomicReference<Map<StopId, String>>,
    executionTreeAtom: AtomicReference<ExecutionTree<T>?>,
    threadSemaphore: Semaphore,
    stateChanged: Channel<Unit>,
    executionNodesAtom: AtomicReference<MutableMap<String, StopExecutionNode<T>>>,
    executionOrderAtom: AtomicReference<MutableList<String>>,
) {
    fun updateExecutionNodeStatus(stop: Stop<T, *>, startTime: Long) {
        // Create execution node
        val executionId = UUID.randomUUID().toString()
        val status = executionStatusAtom.get()[stop] ?: ExecutionStatus.Pending
        val failureReason = failureReasonsAtom.get()[stop.stopId]
        val durationMs = System.currentTimeMillis() - startTime
        
        val executionNode = StopExecutionNode(
            executionId = executionId,
            stop = stop,
            stopId = stop.stopId,
            status = status,
            dependencyStopIds = stop.dependsOn.mapNotNull { passengerId ->
                // Find which stops produce this passenger
                context.route.stops.find { it.produces.contains(passengerId) }?.stopId
            },
            failureReason = failureReason,
            durationMs = durationMs
        )
        
        executionNodesAtom.get()[executionId] = executionNode
        executionOrderAtom.get().add(executionId)
    }

    suspend fun executeStop(stop: Stop<T, *>, startTime: Long = System.currentTimeMillis()) {
        threadSemaphore.acquire()
        var manuallyUpdatedExecutionNodeStatus = false
        try {
            val result = handleStop(stop, context)
            
            when (result) {
                is StopExecutionResult.FanOut<*, *> -> {
                    val fanOutContexts = result.contexts as List<RouteHandlerContext<T, K>>
                    // log the node results before proceeding
                    updateExecutionNodeStatus(stop, startTime)
                    manuallyUpdatedExecutionNodeStatus = true

                    coroutineScope {
                        fanOutContexts.forEach { fanOutContext ->
                            launch {
                                val branchBoardedPassengerIdsAtom =
                                    AtomicReference(boardedPassengerIdsAtom.get().toSet())

                                branchBoardedPassengerIdsAtom.getAndUpdate { it + stop.produces }

                                val branchExecutionStatusAtom =
                                    AtomicReference(executionStatusAtom.get().toMutableMap())

                                branchExecutionStatusAtom.get()[stop] = ExecutionStatus.Completed

                                val branchFailureReasonsAtom =
                                    AtomicReference<Map<StopId, String>>(emptyMap())

                                val branchExecutionTreeAtom = AtomicReference<ExecutionTree<T>?>(null)

                                val branchSemaphore = Semaphore(THREAD_LIMIT)
                                val branchStateChanged = Channel<Unit>(Channel.UNLIMITED)

                                routeStopsWithState(
                                    fanOutContext,
                                    branchBoardedPassengerIdsAtom,
                                    branchExecutionStatusAtom,
                                    branchFailureReasonsAtom,
                                    branchExecutionTreeAtom,
                                    branchSemaphore,
                                    branchStateChanged,
                                    executionNodesAtom,
                                    executionOrderAtom,
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

            if (!manuallyUpdatedExecutionNodeStatus) {
                updateExecutionNodeStatus(stop, startTime)
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

    // Build execution tree from collected nodes
    val tree = ExecutionTree(
        nodes = executionNodesAtom.get(),
        executionOrder = executionOrderAtom.get()
    )
    executionTreeAtom.set(tree)

    if (!stateChanged.isClosedForSend) {
        stateChanged.close()
    }
}