package org.jgrahn.pattern.domain.derived

import org.jgrahn.pattern.DerivedResult
import org.jgrahn.pattern.domain.StudentRosterAccumulator

data class StudentRosterAccumulatorComputeResult(
    val accumulator: StudentRosterAccumulator,
) : DomainDerivedResult