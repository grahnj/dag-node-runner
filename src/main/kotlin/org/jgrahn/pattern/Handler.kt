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
    fun handleIterable(stop: IterableStop<T, K>,
                        context: RouteHandlerContext<T, K>) : List<RouteHandlerContext<T, K>>
    fun handleConditional(stop: ConditionalStop<T, K>,
                          manager: K,
                          hooks: InteractionHooks) : ConditionalResult<K>
}

interface ActionResultHandler<K: PassengerList> {
    fun handleCommandResult(result: CommandResult, manager: K) : K
    fun handleQueryResult(result: QueryResult, manager: K): K
    fun handleDerivedResult(result: DerivedResult, manager: K): K
}

sealed interface StopExecutionResult<T, K: PassengerList> {
    data class Single<T, K: PassengerList>(val result: Result) : StopExecutionResult<T, K>
    data class FanOut<T, K: PassengerList>(val contexts: List<RouteHandlerContext<T, K>>) : StopExecutionResult<T, K>
    data class Conditional<T, K: PassengerList>(val result: ConditionalResult<K>) : StopExecutionResult<T, K>
}

fun <T, K: PassengerList> handleStop(stop: Stop<T, *>, context: RouteHandlerContext<T, K>): StopExecutionResult<T, K> {
    val stopHandler = context.routeHandler.stopHandler

    return when (stop) {
        is CommandStop<*> -> StopExecutionResult.Single(stopHandler.handleCommand(stop as CommandStop<T>, context.passengerList, context.hooks))
        is QueryStop<*>   -> StopExecutionResult.Single(stopHandler.handleQuery(stop as QueryStop<T>, context.passengerList, context.hooks))
        is DerivedStop<*> -> StopExecutionResult.Single(stopHandler.handleDerived(stop as DerivedStop<T>, context.passengerList, context.hooks))
        is IterableStop<*, *> -> StopExecutionResult.FanOut(stopHandler.handleIterable(stop as IterableStop<T, K>, context))
        is ConditionalStop<*, *> -> StopExecutionResult.Conditional(stopHandler.handleConditional(stop as ConditionalStop<T, K>, context.passengerList, context.hooks))
    }
}

fun <K: PassengerList> handleActionResult(actionResult: ActionResult, manager: K, handler: ActionResultHandler<K>): PassengerList {
    return when (actionResult) {
        is CommandResult -> handler.handleCommandResult(actionResult, manager)
        is DerivedResult -> handler.handleDerivedResult(actionResult, manager)
        is QueryResult -> handler.handleQueryResult(actionResult, manager)
    }
}