package org.jgrahn.pattern.domain.derived

import org.jgrahn.pattern.DerivedStop
import org.jgrahn.pattern.StopId
import org.jgrahn.pattern.domain.DomainStopId
import org.jgrahn.pattern.domain.PassengerId

sealed class DomainDerivedStop(
    override val stopId: StopId,
    override val produces: Set<PassengerId>,
    override val dependsOn: Set<PassengerId>
) : DerivedStop<PassengerId>(
    stopId, produces, dependsOn
)

data object StudentRosterAccumulatorComputeStop : DomainDerivedStop(
    stopId = DomainStopId.StudentRosterAccumulatorComputeStop,
    dependsOn = setOf(
        PassengerId.AllActiveStudentList,
        PassengerId.AllClassroomsList,
    ),
    produces = setOf(
        PassengerId.StudentRosterAccumulator
    ),
)