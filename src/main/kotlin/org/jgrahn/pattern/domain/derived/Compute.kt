package org.jgrahn.pattern.domain.derived

import org.jgrahn.pattern.Derivative
import org.jgrahn.pattern.domain.query.Classroom
import org.jgrahn.pattern.domain.query.Student

data class StudentRosterAccumulatorComputeRequest(
    val allStudents: List<Student>,
    val allClassrooms: List<Classroom>,
    ) : Derivative<StudentRosterAccumulatorComputeResult>