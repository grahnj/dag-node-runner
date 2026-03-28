package org.jgrahn.pattern

enum class StopStatus(
    val isRunnable: Boolean,
) {
    Pending(true),
    InProgress(false),
    Failed(false),
    Completed(false),
}

enum class StopId {
    BuildClassroomCommand,
    FindClassroomId,
    BuildStudentRosterCommand,
}

sealed interface Stop<T> {
    val produces: Set<T>
    val dependsOn: Set<T>
}

open class CommandStop<T>(
    override val produces: Set<T>,
    override val dependsOn: Set<T>,
) : Stop<T>

open class QueryStop<T>(
    override val produces: Set<T>,
    override val dependsOn: Set<T>,
) : Stop<T>


interface DerivedStop<T> : Stop<T>


