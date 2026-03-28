package org.jgrahn.pattern.domain

import org.jgrahn.pattern.DerivedStop
import org.jgrahn.pattern.InteractionHooks
import org.jgrahn.pattern.PassengerList
import org.jgrahn.pattern.QueryStop

fun handleDomainCommandStop(
    stop: DomainCommandStop,
    manager: PassengerListManager,
    hooks: InteractionHooks
) {
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
            /// hmmmm, I want a result, but I also don't want to deal with this bullshit
            it.runTyped { hooks.runCommand(it) }
        }
}

fun handleDomainQueryStop(
    stop: DomainQueryStop,
    manager: PassengerListManager,
    interactionHooks: InteractionHooks,
    manager2: PassengerListManager
) {
}

fun handleDomainDerivedStop(
    stop: DerivedStop<PassengerId>,
    manager: PassengerListManager,
    interactionHooks: InteractionHooks,
    manager2: PassengerListManager
) {
}

fun handleDomainCommandResult(result: DomainCommandResult, manager: PassengerListManager): PassengerListManager {
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