package org.jgrahn.pattern

interface CommandResultData

sealed interface ActionResult : Result.Success

interface CommandResult : ActionResult
interface QueryResult : ActionResult
interface DerivedResult : ActionResult

interface Command<R: CommandResult>
interface Query<R: QueryResult>
interface Derivative<R: DerivedResult>