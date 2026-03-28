package org.jgrahn.pattern.domain

import org.jgrahn.pattern.Command

data class BuildClassroomCommand(val classroomId: Long): Command<BuildClassroomResult>
data class BuildStudentRosterCommand(val classroomId: Long): Command<BuildClassroomResult>