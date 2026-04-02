package org.jgrahn.pattern.domain

import org.jgrahn.pattern.PassengerList
import org.jgrahn.pattern.domain.query.Classroom
import org.jgrahn.pattern.domain.query.Student

typealias ClassroomId = Long

data class StudentRosterAccumulator(
    val accum: Map<ClassroomId, List<Student>>
)

data class PassengerListManager(
    var boardedPassengerIds: Set<PassengerId> = emptySet(),
    var classroomId: Long? = null,
    var classroomLocationId: Long? = null,
    var classroom: Any? = null,
    var studentRosterList: List<Student>? = null,
    var allActiveStudents: List<Student>? = null,
    var allClassrooms: List<Classroom>? = null,
    var studentRosterAccumulator: StudentRosterAccumulator? = null,
) : PassengerList

enum class PassengerId {
    AllActiveStudentList,
    AllClassroomsList,
    ClassroomId,
    ClassroomLocationId,
    Classroom,
    StudentRosterList,
    StudentRosterAccumulator,
}