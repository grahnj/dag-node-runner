package org.jgrahn.pattern.domain

import org.jgrahn.pattern.PassengerList
import org.jgrahn.pattern.domain.query.Student

data class StudentRosterAccumulator(
    val accum: Map<Long, List<Student>>
)

data class PassengerListManager(
    var boardedPassengerIds: Set<PassengerId> = emptySet(),
    var classroomId: Long? = null,
    var classroomLocationId: Long? = null,
    var classroom: Any? = null,
    var studentRosterList: Any? = null,
    var allActiveStudents: List<Student>? = null,
    var studentRosterAccumulator: StudentRosterAccumulator? = null,
) : PassengerList

enum class PassengerId {
    ClassroomId,
    ClassroomLocationId,
    Classroom,
    StudentRosterList,
    AllActiveStudentList,
    StudentRosterAccumulator
}