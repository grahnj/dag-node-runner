package org.jgrahn.pattern

interface CommandResult
interface QueryResult

interface Command<R: CommandResult> {
    fun runTyped(execute: () -> R): R = execute()
}

interface Query<R: QueryResult> {
    fun runTyped(execute: () -> R): R = execute()
}