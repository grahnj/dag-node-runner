package org.jgrahn.pattern.domain.command

import org.jgrahn.pattern.InteractionHooks
import org.jgrahn.pattern.Result
import org.jgrahn.pattern.domain.PassengerListManager

fun routeDomainCommandStop(
    stop: DomainCommandStop,
    manager: PassengerListManager,
    hooks: InteractionHooks
) : Result =
    when (stop) {
        is CreateClassroomCommandStop -> {
            CreateClassroomCommand(
                classroomId = manager.classroomId!!,
            )
        }
        is CreateStudentRosterCommandStop -> {
            CreateStudentRosterCommand(
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