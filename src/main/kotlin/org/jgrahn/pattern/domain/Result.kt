package org.jgrahn.pattern.domain

import org.jgrahn.pattern.Result

sealed interface DomainResultData
interface DomainCommandResultData : DomainResultData
interface DomainQueryResultData : DomainResultData

typealias DomainCommandResult = Result<DomainCommandResultData>
typealias DomainQueryResult = Result<DomainQueryResultData>

fun doSomething(result: DomainCommandResult) {
    when (result) {
        is Result.Success<DomainCommandResultData> -> {
            when (val data = result.data) {
                is BuildClassroomResult -> {}
            }
        }
        is Result.Failure -> {

        }
    }
}

val fail = Result.Failure("Something went wrong", Error())

val result = doSomething(fail)

data class BuildClassroomResult(
    val classroom: Any,
) : DomainCommandResultData

data class BuildStudentRosterResult(
    val studentRoster: Any,
) : DomainCommandResultData