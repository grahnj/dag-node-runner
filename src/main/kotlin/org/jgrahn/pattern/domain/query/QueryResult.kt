package org.jgrahn.pattern.domain.query

import org.jgrahn.pattern.QueryResult

sealed interface DomainQueryResult : QueryResult

data class Student(
    val id: Long,
    val name: String,
)

data class AllActiveStudentListResult(
    val activeStudents: List<Student>
) : DomainQueryResult