package org.jgrahn.pattern

interface StopId

sealed interface Stop<T, out Output> {
    val stopId: StopId
    val produces: Set<T>
    val dependsOn: Set<T>
}

data class RouteHandlerContext<T, K: PassengerList>(
    val initialPassengerIdSet: Set<T>,
    val passengerList: K,
    val stops: Set<Stop<T, *>>,
    val hooks: InteractionHooks,
    val routeHandler: RouteHandler<T, K>,
)

open class IterableStop<T, K: PassengerList>(
    override val stopId: StopId,
    override val produces: Set<T>,
    override val dependsOn: Set<T>,
) : Stop<T, List<RouteHandlerContext<T, K>>>

open class ConditionalStop<T, K: PassengerList>(
    override val stopId: StopId,
    override val produces: Set<T>,
    override val dependsOn: Set<T>,
) : Stop<T, ConditionalResult<K>>

open class CommandStop<T>(
    override val stopId: StopId,
    override val produces: Set<T>,
    override val dependsOn: Set<T>,
) : Stop<T, Result>

open class QueryStop<T>(
    override val stopId: StopId,
    override val produces: Set<T>,
    override val dependsOn: Set<T>,
) : Stop<T, Result>


open class DerivedStop<T>(
    override val stopId: StopId,
    override val produces: Set<T>,
    override val dependsOn: Set<T>,
) : Stop<T, Result>


