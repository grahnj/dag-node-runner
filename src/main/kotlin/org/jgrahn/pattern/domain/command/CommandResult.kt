package org.jgrahn.pattern.domain.command

import org.jgrahn.pattern.CommandResult

sealed interface DomainCommandResult : CommandResult

data class BuildClassroomResult(
    val classroom: Any,
) : DomainCommandResult

data class BuildStudentRosterResult(
    val studentRoster: Any,
) : DomainCommandResult