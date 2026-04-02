package org.jgrahn.pattern.domain

import org.jgrahn.pattern.domain.command.BuildStudentRosterResult
import org.jgrahn.pattern.domain.command.CreateStudentRosterCommandStop
import org.jgrahn.pattern.domain.derived.StudentRosterAccumulatorComputeStop
import org.jgrahn.pattern.domain.iterable.IterateStudentRosterAccumulatorStop
import org.jgrahn.pattern.domain.query.FindAllActiveStudentsStop
import org.jgrahn.pattern.domain.query.FindAllClassroomsStop

data object DomainBus {
    val assignStudentsToClassrooms = setOf(
        FindAllClassroomsStop,
        FindAllActiveStudentsStop,
        StudentRosterAccumulatorComputeStop,
        IterateStudentRosterAccumulatorStop,
        CreateStudentRosterCommandStop
        )
}

