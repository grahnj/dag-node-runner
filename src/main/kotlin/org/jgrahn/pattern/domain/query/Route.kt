package org.jgrahn.pattern.domain.query

import org.jgrahn.pattern.Result
import org.jgrahn.pattern.InteractionHooks
import org.jgrahn.pattern.domain.PassengerListManager

fun routeDomainQueryStop(
    stop: DomainQueryStop,
    manager: PassengerListManager,
    hooks: InteractionHooks,
) : Result =
    when (stop) {
        is FindAllActiveStudentsStop -> {
            FindAllActiveStudentsQuery
        }
    }
        .let {
            hooks.runQuery(it)
        }

fun routeDomainQueryResult(
    result: DomainQueryResult,
    manager: PassengerListManager,
) =
    when (result) {
        is AllActiveStudentListResult -> {
            manager.allActiveStudents = result.activeStudents
        }
    }