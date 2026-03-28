package org.jgrahn.pattern.domain

import org.jgrahn.pattern.PassengerList

data class PassengerListManager(
    var boardedPassengerIds: Set<PassengerId> = emptySet(),
    var classroomId: Long? = null,
    var classroomLocationId: Long? = null,
    var classroom: Any? = null,
    var studentRosterList: Any? = null,
) : PassengerList

enum class PassengerId {
    ClassroomId,
    ClassroomLocationId,
    Classroom,
    StudentRosterList,
}