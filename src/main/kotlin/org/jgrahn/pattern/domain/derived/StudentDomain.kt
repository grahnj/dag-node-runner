package org.jgrahn.pattern.domain.derived

interface StudentDomainCompute {
    fun accumulateStudentRoster(
        studentRoster: StudentRosterAccumulatorCompute,
    ): StudentRosterAccumulatorComputeResult {
        TODO("This actually should do something semi-complex")
    }
}

data object StudentDomain
    : StudentDomainCompute