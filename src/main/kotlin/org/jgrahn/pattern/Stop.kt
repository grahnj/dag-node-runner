package org.jgrahn.pattern

interface StopId

sealed interface Stop<T> {
    val stopId: StopId
    val produces: Set<T>
    val dependsOn: Set<T>
}

open class CommandStop<T>(
    override val stopId: StopId,
    override val produces: Set<T>,
    override val dependsOn: Set<T>,
) : Stop<T>

open class QueryStop<T>(
    override val stopId: StopId,
    override val produces: Set<T>,
    override val dependsOn: Set<T>,
) : Stop<T>


open class DerivedStop<T>(
    override val stopId: StopId,
    override val produces: Set<T>,
    override val dependsOn: Set<T>,
) : Stop<T>


