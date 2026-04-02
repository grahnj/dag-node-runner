package org.jgrahn.pattern.domain.iterable

import org.jgrahn.pattern.IterableStop
import org.jgrahn.pattern.domain.DomainStopId
import org.jgrahn.pattern.domain.PassengerId
import org.jgrahn.pattern.domain.PassengerListManager

sealed class DomainIterableStop(
    override val stopId: DomainStopId,
    override val produces: Set<PassengerId>,
    override val dependsOn: Set<PassengerId>
) : IterableStop<PassengerId, PassengerListManager>(
    stopId, produces, dependsOn
)

data object IterateStudentRosterAccumulatorStop : DomainIterableStop(
    stopId = DomainStopId.IterateStudentRosterAccumulatorStop,
    dependsOn = setOf(
        PassengerId.StudentRosterAccumulator,
    ),
    produces = setOf(
        PassengerId.ClassroomId,
        PassengerId.StudentRosterList,
    )
)

