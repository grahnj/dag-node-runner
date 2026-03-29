package org.jgrahn.pattern.domain

import org.jgrahn.pattern.ActionResultHandler
import org.jgrahn.pattern.CommandResult
import org.jgrahn.pattern.CommandStop
import org.jgrahn.pattern.DerivedResult
import org.jgrahn.pattern.DerivedStop
import org.jgrahn.pattern.InteractionHooks
import org.jgrahn.pattern.QueryResult
import org.jgrahn.pattern.QueryStop
import org.jgrahn.pattern.Result
import org.jgrahn.pattern.RouteHandler
import org.jgrahn.pattern.RouteHandlerContext
import org.jgrahn.pattern.StopHandler
import org.jgrahn.pattern.domain.command.DomainCommandResult
import org.jgrahn.pattern.domain.command.DomainCommandStop
import org.jgrahn.pattern.domain.command.routeDomainCommandResult
import org.jgrahn.pattern.domain.command.routeDomainCommandStop


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
                Result.Failure("Unknown command: ${stop.javaClass.canonicalName}", NotImplementedError())
            }
        }


    override fun handleQuery(
        stop: QueryStop<PassengerId>,
        manager: PassengerListManager,
        hooks: InteractionHooks
    ): Result {
        TODO("Not yet implemented")
    }

    override fun handleDerived(
        stop: DerivedStop<PassengerId>,
        manager: PassengerListManager,
        hooks: InteractionHooks
    ): Result {
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
                throw NotImplementedError("This result was configured incorrectly: $result")
            }
        }


    override fun handleQueryResult(
        result: QueryResult,
        manager: PassengerListManager
    ): PassengerListManager {
        TODO("Not yet implemented")
    }

    override fun handleDerivedResult(
        result: DerivedResult,
        manager: PassengerListManager
    ): PassengerListManager {
        TODO("Not yet implemented")
    }

}

object DomainRouteHandler : RouteHandler<PassengerId, PassengerListManager> {
    override val stopHandler: StopHandler<PassengerId, PassengerListManager>
        get() = DomainStopHandler
    override val resultHandler: ActionResultHandler<PassengerListManager>
        get() = DomainActionResultHandler
}

val exampleContext = RouteHandlerContext<PassengerId, PassengerListManager>(
    routeHandler = DomainRouteHandler,
    hooks = TODO(),
    passengerList = PassengerListManager(),
    stops = emptySet(),
    initialPassengerIdSet = emptySet(),
)