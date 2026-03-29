package org.jgrahn.pattern.domain.command

import org.jgrahn.pattern.InteractionHooks
import org.jgrahn.pattern.Result
import org.jgrahn.pattern.domain.BuildClassroomCommandStop
import org.jgrahn.pattern.domain.BuildStudentRosterCommandStop
import org.jgrahn.pattern.domain.DomainCommandStop
import org.jgrahn.pattern.domain.PassengerListManager

fun routeDomainCommandStop(
    stop: DomainCommandStop,
    manager: PassengerListManager,
    hooks: InteractionHooks
) : Result =
    when (stop) {
        is BuildClassroomCommandStop -> {
            BuildClassroomCommand(
                classroomId = manager.classroomId!!,
            )
        }
        is BuildStudentRosterCommandStop -> {
            BuildStudentRosterCommand(
                classroomId = manager.classroomId!!,
            )
        }
    }
        .let {
            hooks.runCommand(it)
        }

fun routeDomainCommandResult(result: DomainCommandResult, manager: PassengerListManager): PassengerListManager {
    when (result) {
        is BuildClassroomResult -> {
            manager.classroom = result.classroom
        }
        is BuildStudentRosterResult -> {
            manager.studentRosterList = result.studentRoster
        }
    }

    return manager
}