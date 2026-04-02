package org.jgrahn.pattern.domain.derived

import org.jgrahn.pattern.domain.StudentRosterAccumulator
import org.jgrahn.pattern.domain.query.Student

interface StudentDomainCompute {
    fun accumulateStudentRoster(
        request: StudentRosterAccumulatorComputeRequest,
    ): StudentRosterAccumulatorComputeResult {
        val totalClassrooms = request.allClassrooms.size
        val totalStudents = request.allStudents.size
        val studentsPerClassroom = totalStudents / totalClassrooms
        val overflow = totalStudents % totalClassrooms

        val shuffledStudents = request.allStudents.shuffled()
        val accumulator = mutableMapOf<Long, List<Student>>()

        var studentIndex = 0
        request.allClassrooms.forEachIndexed { classroomIndex, classroom ->
            val studentsForThisClassroom = studentsPerClassroom + (if (classroomIndex < overflow) 1 else 0)
            val classroomStudents = shuffledStudents.subList(studentIndex, studentIndex + studentsForThisClassroom)
            accumulator[classroom.id] = classroomStudents
            studentIndex += studentsForThisClassroom
        }

        return StudentRosterAccumulatorComputeResult(
            accumulator = StudentRosterAccumulator(accumulator)
        )
    }
}

data object StudentDomain
    : StudentDomainCompute