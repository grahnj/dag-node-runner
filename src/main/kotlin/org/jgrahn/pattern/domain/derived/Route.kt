package org.jgrahn.pattern.domain.derived

import org.jgrahn.pattern.Result
import org.jgrahn.pattern.domain.PassengerListManager

fun routeDomainDerivedStop(
    stop: DomainDerivedStop,
    manager: PassengerListManager,
) : Result =
    when (stop) {
        StudentRosterAccumulatorComputeStop -> {
            val allStudents = manager.allActiveStudents
            val allClassrooms = manager.allClassrooms

            checkNotNull(allStudents)
            checkNotNull(allClassrooms)

            val request = StudentRosterAccumulatorComputeRequest(
                allStudents = allStudents,
                allClassrooms = allClassrooms,
            )

            StudentDomain.accumulateStudentRoster(request)
        }
    }

fun routeDomainDerivedResult(
    result: DomainDerivedResult,
    manager: PassengerListManager,
) : PassengerListManager =
    when (result) {
        is StudentRosterAccumulatorComputeResult -> {
            manager.apply {
                studentRosterAccumulator = result.accumulator
            }
        }
    }