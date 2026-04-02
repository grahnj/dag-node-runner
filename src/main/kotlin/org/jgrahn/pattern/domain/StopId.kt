package org.jgrahn.pattern.domain

import org.jgrahn.pattern.StopId

enum class DomainStopId : StopId {
    CreateClassroomStop,
    CreateStudentRosterStop,
    FindAllActiveStudentsStop,
    FindAllClassroomsStop,
    IterateStudentRosterAccumulatorStop,
    StudentRosterAccumulatorComputeStop,
}