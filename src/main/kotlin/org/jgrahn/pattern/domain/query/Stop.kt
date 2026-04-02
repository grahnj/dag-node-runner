package org.jgrahn.pattern.domain.query

import org.jgrahn.pattern.QueryStop
import org.jgrahn.pattern.StopId
import org.jgrahn.pattern.domain.DomainStopId
import org.jgrahn.pattern.domain.PassengerId

sealed class DomainQueryStop(
    override val stopId: StopId,
    override val produces: Set<PassengerId>,
    override val dependsOn: Set<PassengerId>
) : QueryStop<PassengerId>(
    stopId, produces, dependsOn
)

data object FindAllActiveStudentsStop
    : DomainQueryStop(
        stopId = DomainStopId.FindAllActiveStudentsStop,
        produces = setOf(PassengerId.AllActiveStudentList),
        dependsOn = emptySet(),
)

data object FindAllClassroomsStop
    : DomainQueryStop(
        stopId = DomainStopId.FindAllClassroomsStop,
        produces = setOf(PassengerId.AllClassroomsList),
        dependsOn = emptySet(),
    )