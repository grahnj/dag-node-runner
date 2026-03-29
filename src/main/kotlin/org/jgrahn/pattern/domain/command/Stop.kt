package org.jgrahn.pattern.domain.command

import org.jgrahn.pattern.CommandStop
import org.jgrahn.pattern.StopId
import org.jgrahn.pattern.domain.DomainStopId
import org.jgrahn.pattern.domain.PassengerId

sealed class DomainCommandStop(
    override val stopId: StopId,
    override val produces: Set<PassengerId>,
    override val dependsOn: Set<PassengerId>
) : CommandStop<PassengerId>(
    stopId, produces, dependsOn
)

data object BuildClassroomCommandStop : DomainCommandStop(
    stopId = DomainStopId.BuildClassroomStop,
    produces = setOf(
        PassengerId.Classroom,
    ),
    dependsOn = setOf(
        PassengerId.ClassroomId,
        PassengerId.ClassroomLocationId,
    )
)

data object BuildStudentRosterCommandStop : DomainCommandStop(
    stopId = DomainStopId.BuildStudentRosterStop,
    produces = setOf(
        PassengerId.StudentRosterList,
    ),
    dependsOn = setOf(
        PassengerId.ClassroomLocationId,
        PassengerId.ClassroomId,
        PassengerId.Classroom,
    )
)