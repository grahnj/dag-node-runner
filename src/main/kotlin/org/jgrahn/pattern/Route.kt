package org.jgrahn.pattern

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore
import java.util.concurrent.atomic.AtomicReference


interface RouteHandler<T> {
    val stopHandler: StopHandler<T>
    val resultHandler: ResultHandler
    fun handleStop(stop: Stop<T>, interactionHooks: InteractionHooks, manager: PassengerList): T
    fun handleResult(result: T, manager: PassengerList): PassengerList
}

interface StopHandler<T> {
    fun handleCommand(stop: CommandStop<T>,
                      manager: PassengerList,
                      hooks: InteractionHooks,)
    fun handleQuery(stop: QueryStop<T>,
                    manager: PassengerList,
                    hooks: InteractionHooks,)
    fun handleDerived(stop: DerivedStop<T>,
                    manager: PassengerList,
                    hooks: InteractionHooks,)
}

interface ResultHandler {
    fun handleCommandResult(result: Result, manager: PassengerList) : PassengerList
    fun handleQueryResult(result: Result, manager: PassengerList): PassengerList
    fun handleDerivedResult(result: Result, manager: PassengerList): PassengerList
}


fun <T> executeStop(stop: Stop<T>, interactionHooks: InteractionHooks, manager: PassengerList, domainHandler: StopHandler<T>): Result {
    when (stop) {
        is CommandStop -> domainHandler.handleCommand(stop, manager, interactionHooks)
        is QueryStop   -> domainHandler.handleQuery(stop, manager, interactionHooks)
        is DerivedStop -> domainHandler.handleDerived(stop, manager, interactionHooks)
    }

    TODO("This is not done yet")
}

fun handleResult(result: Result, manager: PassengerList, handler: ResultHandler): PassengerList {
    return when (result) {
        is CommandResult -> handler.handleCommandResult(result, manager)
    }
}

data class RouteHandlerContext<T>(
    val manager: PassengerList,
    val stops: Set<Stop<T>>,
    val hooks: InteractionHooks,
    val routeHandler: RouteHandler<T>,
)

const val THREAD_LIMIT = 3

suspend fun <T> routeStops(context: RouteHandlerContext<T>) {
    val stops = context.stops
    val handler = context.routeHandler
    val managerAtom = AtomicReference(context.manager)
    val stopStatusAtom = AtomicReference(stops.associateWith { StopStatus.Pending })

    val threadSemaphore = Semaphore(THREAD_LIMIT)

    val stateChanged = Channel<Unit>(Channel.UNLIMITED)

//    stops
//        .filter { it.stopStatus.isRunnable }
//        .filter { it.dependsOn.all { passengerId -> manager.boardedPassengerIds.contains(passengerId) } }
//        .forEach {
//            it.stopStatus = StopStatus.InProgress
//            // this is dumb to do this right here, the control flow should be ours not the callers
//            // this is going to get run async
//            // TODO: something is off here, but it's not clicking for me
//            val result = handler.handleStop(it, hooks, manager)
//
//            when (result) {
//                is Result.Success -> {
//                    // TODO: Handle success results
//                }
//                is Result.Failure -> {
//                    // TODO: Handle this failure
//                }
//            }
//            // put the result into the passenger list
//            // update the stop status to be completed or failed depending on the result
//            // (our internal executor should handle the stop status stuff)
//        }
}