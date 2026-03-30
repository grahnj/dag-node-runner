package org.jgrahn.pattern.domain.iterable

import org.jgrahn.pattern.domain.DomainRouteHandlerContext

fun routeDomainIterableStop(
    stop: DomainIterableStop,
    context: DomainRouteHandlerContext,
) : List<DomainRouteHandlerContext> =
    when (stop) {
        IterateStudentRosterAccumulatorStop -> {
            val accum = context.passengerList.studentRosterAccumulator

            checkNotNull(accum)

            accum.accum
                .map {
                    context.copy(
                        passengerList = context.passengerList.copy(
                            classroomId = it.key,
                            studentRosterList = it.value
                        )
                    )
                }
        }
    }