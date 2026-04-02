package org.jgrahn.pattern.domain

import org.jgrahn.pattern.ActionResultHandler
import org.jgrahn.pattern.CommandResult
import org.jgrahn.pattern.CommandStop
import org.jgrahn.pattern.ConditionalResult
import org.jgrahn.pattern.ConditionalStop
import org.jgrahn.pattern.DerivedResult
import org.jgrahn.pattern.DerivedStop
import org.jgrahn.pattern.InteractionHooks
import org.jgrahn.pattern.IterableStop
import org.jgrahn.pattern.QueryResult
import org.jgrahn.pattern.QueryStop
import org.jgrahn.pattern.Result
import org.jgrahn.pattern.Route
import org.jgrahn.pattern.RouteHandler
import org.jgrahn.pattern.RouteHandlerContext
import org.jgrahn.pattern.StopHandler
import org.jgrahn.pattern.domain.command.DomainCommandResult
import org.jgrahn.pattern.domain.command.DomainCommandStop
import org.jgrahn.pattern.domain.command.routeDomainCommandResult
import org.jgrahn.pattern.domain.command.routeDomainCommandStop
import org.jgrahn.pattern.domain.derived.DomainDerivedResult
import org.jgrahn.pattern.domain.derived.DomainDerivedStop
import org.jgrahn.pattern.domain.derived.routeDomainDerivedResult
import org.jgrahn.pattern.domain.derived.routeDomainDerivedStop
import org.jgrahn.pattern.domain.iterable.DomainIterableStop
import org.jgrahn.pattern.domain.iterable.routeDomainIterableStop
import org.jgrahn.pattern.domain.query.DomainQueryResult
import org.jgrahn.pattern.domain.query.DomainQueryStop
import org.jgrahn.pattern.domain.query.routeDomainQueryResult
import org.jgrahn.pattern.domain.query.routeDomainQueryStop

// FIXME: There are two patterns here, we probably need to use the more verbose one
//        so that we can wrap everything in a result
object DomainStopHandler : StopHandler<PassengerId, PassengerListManager> {
    override fun handleCommand(
        stop: CommandStop<PassengerId>,
        manager: PassengerListManager,
        hooks: InteractionHooks
    ): Result =
        when (stop) {
            is DomainCommandStop -> {
                routeDomainCommandStop(stop, manager, hooks)
            }
            else -> {
                Result.Failure("Unknown command stop: ${stop.javaClass.canonicalName}", NotImplementedError())
            }
        }


    override fun handleQuery(
        stop: QueryStop<PassengerId>,
        manager: PassengerListManager,
        hooks: InteractionHooks
    ): Result =
        when (stop) {
            is DomainQueryStop -> {
                routeDomainQueryStop(stop, manager, hooks)
            }
            else -> {
                Result.Failure("Unknown query stop: ${stop.javaClass.canonicalName}", NotImplementedError())
            }
        }

    override fun handleDerived(
        stop: DerivedStop<PassengerId>,
        manager: PassengerListManager,
        hooks: InteractionHooks
    ): Result =
        routeDomainDerivedStop(stop as DomainDerivedStop, manager)


    override fun handleIterable(
        stop: IterableStop<PassengerId, PassengerListManager>,
        context: RouteHandlerContext<PassengerId, PassengerListManager>,
    ): List<RouteHandlerContext<PassengerId, PassengerListManager>> =
        routeDomainIterableStop(stop as DomainIterableStop, context)


    override fun handleConditional(
        stop: ConditionalStop<PassengerId, PassengerListManager>,
        manager: PassengerListManager,
        hooks: InteractionHooks
    ): ConditionalResult<PassengerListManager> {
        TODO("Not yet implemented")
    }

}

object DomainActionResultHandler : ActionResultHandler<PassengerListManager> {
    override fun handleCommandResult(
        result: CommandResult,
        manager: PassengerListManager
    ): PassengerListManager =
        when (result) {
            is DomainCommandResult -> {
                routeDomainCommandResult(result, manager)
            }
            else -> {
                throw NotImplementedError("This command result was configured incorrectly: $result")
            }
        }


    override fun handleQueryResult(
        result: QueryResult,
        manager: PassengerListManager
    ): PassengerListManager =
        when (result) {
            is DomainQueryResult -> {
                routeDomainQueryResult(result, manager)
            }
            else -> {
                throw NotImplementedError("This query result was configured incorrectly: $result")
            }
        }


    override fun handleDerivedResult(
        result: DerivedResult,
        manager: PassengerListManager
    ): PassengerListManager =
        when (result) {
            is DomainDerivedResult -> {
                routeDomainDerivedResult(result, manager)
            }
            else -> {
                throw NotImplementedError("This derived result was configured incorrectly: $result")
            }
        }

}

object DomainRouteHandler : RouteHandler<PassengerId, PassengerListManager> {
    override val stopHandler: StopHandler<PassengerId, PassengerListManager>
        get() = DomainStopHandler
    override val resultHandler: ActionResultHandler<PassengerListManager>
        get() = DomainActionResultHandler
}

val exampleContext = RouteHandlerContext(
    routeHandler = DomainRouteHandler,
    hooks = TODO(),
    passengerList = PassengerListManager(),
    route = Route(
        stops = emptySet()
    ),
    initialPassengerIdSet = emptySet(),
)