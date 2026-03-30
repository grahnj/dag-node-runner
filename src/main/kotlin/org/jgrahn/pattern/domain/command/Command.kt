package org.jgrahn.pattern.domain.command

import org.jgrahn.pattern.Command

data class CreateClassroomCommand(val classroomId: Long): Command<BuildClassroomResult>
data class CreateStudentRosterCommand(val classroomId: Long): Command<BuildClassroomResult>