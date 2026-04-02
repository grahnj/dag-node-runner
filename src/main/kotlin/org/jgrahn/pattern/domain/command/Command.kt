package org.jgrahn.pattern.domain.command

import org.jgrahn.pattern.Command
import org.jgrahn.pattern.domain.query.Student

data class CreateClassroomCommand(val classroomId: Long): Command<BuildClassroomResult>
data class CreateStudentRosterCommand(val classroomId: Long, val studentRosterList: List<Student>): Command<BuildClassroomResult>