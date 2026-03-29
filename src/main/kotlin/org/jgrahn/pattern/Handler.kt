package org.jgrahn.pattern

interface RouteHandler<T, K: PassengerList> {
    val stopHandler: StopHandler<T, K>
    val resultHandler: ActionResultHandler<K>
}

interface StopHandler<T, K: PassengerList> {
    fun handleCommand(stop: CommandStop<T>,
                      manager: K,
                      hooks: InteractionHooks,) : Result
    fun handleQuery(stop: QueryStop<T>,
                    manager: K,
                    hooks: InteractionHooks,) : Result
    fun handleDerived(stop: DerivedStop<T>,
                      manager: K,
                      hooks: InteractionHooks,) : Result
}

interface ActionResultHandler<K: PassengerList> {
    fun handleCommandResult(result: CommandResult, manager: K) : K
    fun handleQueryResult(result: QueryResult, manager: K): K
    fun handleDerivedResult(result: DerivedResult, manager: K): K
}


fun <T, K: PassengerList> handleStop(stop: Stop<T>, context: RouteHandlerContext<T, K>): Result {
    val stopHandler = context.routeHandler.stopHandler

    return when (stop) {
        is CommandStop -> stopHandler.handleCommand(stop, context.passengerList, context.hooks)
        is QueryStop   -> stopHandler.handleQuery(stop, context.passengerList, context.hooks)
        is DerivedStop -> stopHandler.handleDerived(stop, context.passengerList, context.hooks)
    }
}

fun <K: PassengerList> handleActionResult(actionResult: ActionResult, manager: K, handler: ActionResultHandler<K>): PassengerList {
    return when (actionResult) {
        is CommandResult -> handler.handleCommandResult(actionResult, manager)
        is DerivedResult -> handler.handleDerivedResult(actionResult, manager)
        is QueryResult -> handler.handleQueryResult(actionResult, manager)
    }
}