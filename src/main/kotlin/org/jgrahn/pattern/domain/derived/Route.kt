package org.jgrahn.pattern.domain.derived

import org.jgrahn.pattern.Result
import org.jgrahn.pattern.domain.PassengerListManager

fun routeDomainDerivedStop(
    stop: DomainDerivedStop,
    manager: PassengerListManager,
) : Result =
    runCatching {
        when (stop) {
            is StudentRosterAccumulatorComputeStop -> {
                val allActiveStudents = manager.allActiveStudents

                checkNotNull(allActiveStudents)

                val compute = StudentRosterAccumulatorCompute(
                    allStudents = allActiveStudents
                )

                StudentDomain.accumulateStudentRoster(compute)
            }
        }
    }.getOrElse {
        Result.Failure(
            "Something went wrong in a derived step, likely related to state",
            Error(it))
    }