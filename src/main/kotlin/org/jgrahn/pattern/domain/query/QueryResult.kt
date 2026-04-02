package org.jgrahn.pattern.domain.query

import org.jgrahn.pattern.QueryResult

sealed interface DomainQueryResult : QueryResult

// FYI: These would really be in an entity folder
data class Student(
    val id: Long,
    val name: String,
)

data class Classroom(
    val id: Long,
    val name: String,
)

data class AllActiveStudentListResult(
    val activeStudents: List<Student>,
) : DomainQueryResult

data class AllClassroomsResult(
    val classrooms: List<Classroom>,
) : DomainQueryResult