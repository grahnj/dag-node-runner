package org.jgrahn.pattern.domain.derived

import org.jgrahn.pattern.Derivative
import org.jgrahn.pattern.domain.query.Student

data class StudentRosterAccumulatorCompute(
    val allStudents: List<Student>,
    ) : Derivative<StudentRosterAccumulatorComputeResult>