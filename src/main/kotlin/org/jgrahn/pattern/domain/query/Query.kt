package org.jgrahn.pattern.domain.query

import org.jgrahn.pattern.Query

data object FindAllClassroomsQuery : Query<AllClassroomsResult>
data object FindAllActiveStudentsQuery : Query<AllActiveStudentListResult>