package org.jgrahn.pattern.domain

import kotlinx.coroutines.runBlocking
import org.jgrahn.pattern.Command
import org.jgrahn.pattern.CommandResult
import org.jgrahn.pattern.InteractionHooks
import org.jgrahn.pattern.Query
import org.jgrahn.pattern.QueryResult
import org.jgrahn.pattern.Result
import org.jgrahn.pattern.Route
import org.jgrahn.pattern.domain.command.BuildStudentRosterResult
import org.jgrahn.pattern.domain.command.CreateStudentRosterCommand
import org.jgrahn.pattern.domain.query.AllActiveStudentListResult
import org.jgrahn.pattern.domain.query.AllClassroomsResult
import org.jgrahn.pattern.domain.query.Classroom
import org.jgrahn.pattern.domain.query.FindAllActiveStudentsQuery
import org.jgrahn.pattern.domain.query.FindAllClassroomsQuery
import org.jgrahn.pattern.domain.query.Student
import org.jgrahn.pattern.routeStops
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DomainBusTest {

    @Nested
    inner class AssignStudentsToClassrooms {

        @Test
        fun `should distribute students evenly across classrooms`() {
            // Arrange
            val testStudents = listOf(
                Student(1L, "Alice"),
                Student(2L, "Bob"),
                Student(3L, "Charlie"),
                Student(4L, "Diana"),
                Student(5L, "Eve"),
                Student(6L, "Frank"),
            )
            val testClassrooms = listOf(
                Classroom(1L, "Room A"),
                Classroom(2L, "Room B"),
                Classroom(3L, "Room C"),
            )

            val createdRosters = mutableListOf<CreateStudentRosterCommand>()

            val stubHooks = object : InteractionHooks {
                override fun <K : QueryResult, T : Query<K>> runQuery(query: T): Result {
                    return when (query) {
                        is FindAllActiveStudentsQuery -> AllActiveStudentListResult(testStudents)
                        is FindAllClassroomsQuery -> AllClassroomsResult(testClassrooms)
                        else -> throw IllegalArgumentException("Unknown query: $query")
                    }
                }

                override fun <K : CommandResult, T : Command<K>> runCommand(command: T): Result {
                    when (command) {
                        is CreateStudentRosterCommand -> {
                            createdRosters.add(command)
                            assertEquals(
                                2,
                                command.studentRosterList.size,
                                "Each roster command should have 2 students (6 / 3 classrooms)"
                            )
                            assertTrue(
                                command.studentRosterList.all { it in testStudents },
                                "All students in roster should be from test data"
                            )
                        }
                    }
                    return BuildStudentRosterResult(Unit)
                }
            }

            val passengerListManager = PassengerListManager()
            val route = Route(DomainBus.assignStudentsToClassrooms)
            val context = DomainRouteHandlerContext(
                initialPassengerIdSet = emptySet(),
                passengerList = passengerListManager,
                route = route,
                hooks = stubHooks,
                routeHandler = DomainRouteHandler,
            )

            // Act
            val result = runBlocking {
                routeStops(context)
            }

            // Assert
            assertTrue(result.success, "Route execution should succeed. Failures: ${result.failureReasons}")
            assertNotNull(context.passengerList.studentRosterAccumulator, "Student roster accumulator should be populated")

            val accumulator = context.passengerList.studentRosterAccumulator!!.accum
            assertEquals(3, accumulator.size, "Should have roster for all 3 classrooms")

            // Verify distribution
            accumulator.values.forEach { classroomStudents ->
                assertEquals(
                    2,
                    classroomStudents.size,
                    "Each classroom should have 2 students (6 students / 3 classrooms)"
                )
            }

            // Verify all students are assigned
            val allAssignedStudents = accumulator.values.flatten()
            assertEquals(6, allAssignedStudents.size, "All 6 students should be assigned")
            assertEquals(
                testStudents.toSet(),
                allAssignedStudents.toSet(),
                "All test students should be assigned"
            )

            // Verify roster commands were executed
            assertEquals(3, createdRosters.size, "Should create 3 roster commands (one per classroom)")
        }

        @Test
        fun `should handle uneven distribution with overflow students`() = runBlocking {
            // Arrange
            val testStudents = listOf(
                Student(1L, "Alice"),
                Student(2L, "Bob"),
                Student(3L, "Charlie"),
                Student(4L, "Diana"),
                Student(5L, "Eve"),
            )
            val testClassrooms = listOf(
                Classroom(1L, "Room A"),
                Classroom(2L, "Room B"),
            )

            val rosterSizes = mutableListOf<Int>()

            val stubHooks = object : InteractionHooks {
                override fun <K : QueryResult, T : Query<K>> runQuery(query: T): Result {
                    return when (query) {
                        is FindAllActiveStudentsQuery -> AllActiveStudentListResult(testStudents)
                        is FindAllClassroomsQuery -> AllClassroomsResult(testClassrooms)
                        else -> throw IllegalArgumentException("Unknown query")
                    }
                }

                override fun <K : CommandResult, T : Command<K>> runCommand(
                    command: T
                ): Result {
                    when (command) {
                        is CreateStudentRosterCommand -> {
                            rosterSizes.add(command.studentRosterList.size)
                        }
                    }
                    return BuildStudentRosterResult(Unit)
                }
            }

            val passengerListManager = PassengerListManager()
            val route = Route(DomainBus.assignStudentsToClassrooms)
            val context = DomainRouteHandlerContext(
                initialPassengerIdSet = emptySet(),
                passengerList = passengerListManager,
                route = route,
                hooks = stubHooks,
                routeHandler = DomainRouteHandler,
            )

            // Act
            val result = routeStops(context)

            // Assert
            assertTrue(result.success, "Route execution should succeed")
            assertNotNull(context.passengerList.studentRosterAccumulator, "Student roster accumulator should be populated")

            val accumulator = context.passengerList.studentRosterAccumulator!!.accum
            assertEquals(2, accumulator.size, "Should have roster for both classrooms")

            val classroomSizes = accumulator.values.map { it.size }
            assertEquals(2, rosterSizes.size, "Should create 2 roster commands")
            assertTrue(
                rosterSizes.contains(3) && rosterSizes.contains(2),
                "First classroom should have 3 students, second should have 2 (5 / 2 with overflow)"
            )

            val allAssignedStudents = accumulator.values.flatten()
            assertEquals(5, allAssignedStudents.size, "All 5 students should be assigned")
        }
    }
}