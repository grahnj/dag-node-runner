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
        CreateClassroomCommandStop -> {
            val classroomId = manager.classroomId

            checkNotNull(classroomId)

            CreateClassroomCommand(
                classroomId = classroomId,
            )
        }
        CreateStudentRosterCommandStop -> {
            val classroomId = manager.classroomId
            val studentRosterList = manager.studentRosterList

            checkNotNull(classroomId)
            checkNotNull(studentRosterList)

            CreateStudentRosterCommand(
                classroomId = classroomId,
                studentRosterList = studentRosterList,
            )
        }
    }
        .let {
            hooks.runCommand(it)
        }

fun routeDomainCommandResult(result: DomainCommandResult, manager: PassengerListManager): PassengerListManager =
    when (result) {
        is BuildClassroomResult -> {
            manager.apply {
                classroom = result.classroom
            }
        }
        is BuildStudentRosterResult -> {
            manager
        }
    }
