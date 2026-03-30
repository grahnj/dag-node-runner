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

//data object FindClassRoomIdStop
//    : DomainQueryStop(
//    stopId = DomainStopId.FindClassroomIdStop,
//    produces = setOf(PassengerId.ClassroomId),
//    dependsOn = emptySet(),
//)

data object FindAllActiveStudentsStop
    : DomainQueryStop(
    stopId = DomainStopId.FindAllActiveStudentsStop,
        produces = setOf(PassengerId.AllActiveStudentList),
        dependsOn = emptySet(),
)