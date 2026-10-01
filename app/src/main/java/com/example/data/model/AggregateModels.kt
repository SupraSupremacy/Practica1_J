package com.example.data.model

data class StudentWithAttendance(
    val student: StudentEntity,
    val record: AttendanceRecordEntity?
)

data class StudentAttendanceSummary(
    val student: StudentEntity,
    val totalSessions: Int,
    val presentCount: Int,
    val lateCount: Int,
    val justifiedCount: Int,
    val absentCount: Int
) {
    val attendancePercentage: Int
        get() = if (totalSessions > 0) {
            val attended = presentCount + (lateCount * 0.8) // Partial weight for late
            ((attended / totalSessions) * 100).toInt().coerceIn(0, 100)
        } else {
            100
        }
}

data class GradeWithStats(
    val grade: GradeEntity,
    val studentCount: Int,
    val sessionCount: Int,
    val averageAttendanceRate: Int
)
