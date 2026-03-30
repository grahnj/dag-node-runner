package org.jgrahn.pattern.domain

import org.jgrahn.pattern.StopId

enum class DomainStopId : StopId {
    CreateClassroomStop,
    CreateStudentRosterStop,
    FindClassroomIdStop,
    FindAllActiveStudentsStop,
    IterateStudentRosterAccumulatorStop,
    StudentRosterAccumulatorComputeStop
}