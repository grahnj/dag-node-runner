package org.jgrahn.pattern

interface InteractionHooks {
    fun <K: CommandResult, T: Command<K>> runCommand(command: T): Result
    fun <K: QueryResult, T: Query<K>> runQuery(query: T): Result
//    fun stopCompletedHook(dagNode: Any): Unit
}