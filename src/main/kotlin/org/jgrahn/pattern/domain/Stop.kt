package org.jgrahn.pattern.domain

import org.jgrahn.pattern.CommandStop
import org.jgrahn.pattern.QueryStop

sealed class DomainCommandStop(
    override val produces: Set<PassengerId>,
    override val dependsOn: Set<PassengerId>
) : CommandStop<PassengerId>(
    produces, dependsOn
)

sealed class DomainQueryStop(
    override val produces: Set<PassengerId>,
    override val dependsOn: Set<PassengerId>
) : QueryStop<PassengerId>(
    produces, dependsOn
)

data object BuildClassroomCommandStop : DomainCommandStop(
    produces = setOf(
        PassengerId.Classroom,
    ),
    dependsOn = setOf(
        PassengerId.ClassroomId,
        PassengerId.ClassroomLocationId,
    )
)

data object BuildStudentRosterCommandStop : DomainCommandStop(
    produces = setOf(
        PassengerId.StudentRosterList,
    ),
    dependsOn = setOf(
        PassengerId.ClassroomLocationId,
        PassengerId.ClassroomId,
        PassengerId.Classroom,
    )
)

data object FindClassRoomIdStop
    : DomainQueryStop(
    produces = setOf(PassengerId.ClassroomId),
    dependsOn = emptySet(),
)

val routeStops = setOf(
    FindClassRoomIdStop,
    BuildStudentRosterCommandStop,
    BuildClassroomCommandStop,
)