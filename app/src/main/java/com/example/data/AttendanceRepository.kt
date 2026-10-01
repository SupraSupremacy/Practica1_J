package com.example.data

import com.example.data.dao.AttendanceDao
import com.example.data.dao.GradeDao
import com.example.data.dao.StudentDao
import com.example.data.dao.TeacherProfileDao
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.GradeEntity
import com.example.data.model.GradeWithStats
import com.example.data.model.StudentAttendanceSummary
import com.example.data.model.StudentDetailedReport
import com.example.data.model.StudentEntity
import com.example.data.model.StudentSessionHistoryItem
import com.example.data.model.StudentWithAttendance
import com.example.data.model.TeacherProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class AttendanceRepository(
    private val gradeDao: GradeDao,
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val teacherProfileDao: TeacherProfileDao
) {
    val allGrades: Flow<List<GradeEntity>> = gradeDao.getAllGrades()
    val activeSession: Flow<AttendanceSessionEntity?> = attendanceDao.getActiveSession()
    val allSessions: Flow<List<AttendanceSessionEntity>> = attendanceDao.getAllSessions()
    val teacherProfile: Flow<TeacherProfileEntity?> = teacherProfileDao.getProfile()
    val totalStudentsCount: Flow<Int> = studentDao.getTotalStudentCount()
    val totalSessionsCount: Flow<Int> = attendanceDao.getTotalSessionsCount()

    // Grades
    fun getGrade(id: Long): Flow<GradeEntity?> = gradeDao.getGradeById(id)

    suspend fun createGrade(name: String, section: String, subject: String, colorHex: String): Long =
        withContext(Dispatchers.IO) {
            gradeDao.insertGrade(
                GradeEntity(
                    name = name.trim(),
                    section = section.trim(),
                    subject = subject.trim(),
                    colorHex = colorHex
                )
            )
        }

    suspend fun updateGrade(grade: GradeEntity) = withContext(Dispatchers.IO) {
        gradeDao.updateGrade(grade)
    }

    suspend fun deleteGrade(gradeId: Long) = withContext(Dispatchers.IO) {
        gradeDao.deleteGradeById(gradeId)
    }

    // Students
    fun getStudentsByGrade(gradeId: Long): Flow<List<StudentEntity>> =
        studentDao.getStudentsByGrade(gradeId)

    suspend fun addStudent(
        gradeId: Long,
        fullName: String,
        studentCode: String,
        email: String = "",
        phone: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val colors = listOf("#1A56DB", "#0E9F6E", "#7E3AF2", "#C27803", "#E02424", "#0694A2", "#D61F69")
        val randomColor = colors.random()
        studentDao.insertStudent(
            StudentEntity(
                gradeId = gradeId,
                fullName = fullName.trim(),
                studentCode = studentCode.trim().uppercase(),
                email = email.trim(),
                phone = phone.trim(),
                avatarColorHex = randomColor
            )
        )
    }

    suspend fun updateStudent(student: StudentEntity) = withContext(Dispatchers.IO) {
        studentDao.updateStudent(student)
    }

    suspend fun deleteStudent(studentId: Long) = withContext(Dispatchers.IO) {
        studentDao.deleteStudentById(studentId)
    }

    // Attendance Summaries per student in a grade
    fun getStudentSummariesForGrade(gradeId: Long): Flow<List<StudentAttendanceSummary>> {
        val studentsFlow = studentDao.getStudentsByGrade(gradeId)
        val sessionsFlow = attendanceDao.getSessionsByGrade(gradeId)
        val recordsFlow = attendanceDao.getRecordsByGrade(gradeId)

        return combine(studentsFlow, sessionsFlow, recordsFlow) { students, sessions, records ->
            val totalSessions = sessions.size
            students.map { student ->
                val studentRecords = records.filter { it.studentId == student.id }
                val present = studentRecords.count { it.status == AttendanceRecordEntity.STATUS_PRESENTE }
                val late = studentRecords.count { it.status == AttendanceRecordEntity.STATUS_RETARDO }
                val justified = studentRecords.count { it.status == AttendanceRecordEntity.STATUS_JUSTIFICADO }
                val absent = totalSessions - (present + late + justified)
                StudentAttendanceSummary(
                    student = student,
                    totalSessions = totalSessions,
                    presentCount = present,
                    lateCount = late,
                    justifiedCount = justified,
                    absentCount = absent.coerceAtLeast(0)
                )
            }
        }
    }

    // Global detailed reports for all students across all grades
    fun getAllStudentsWithDetailedReports(): Flow<List<StudentDetailedReport>> {
        val studentsFlow = studentDao.getAllStudents()
        val gradesFlow = gradeDao.getAllGrades()
        val sessionsFlow = attendanceDao.getAllSessions()
        val recordsFlow = attendanceDao.getAllRecords()

        return combine(studentsFlow, gradesFlow, sessionsFlow, recordsFlow) { students, grades, sessions, records ->
            val gradeMap = grades.associateBy { it.id }
            val sessionsByGrade = sessions.groupBy { it.gradeId }
            val recordsByStudent = records.groupBy { it.studentId }

            students.map { student ->
                val grade = gradeMap[student.gradeId]
                val gradeSessions = sessionsByGrade[student.gradeId] ?: emptyList()
                val studentRecords = recordsByStudent[student.id] ?: emptyList()
                val recordMap = studentRecords.associateBy { it.sessionId }

                val historyItems = gradeSessions.map { session ->
                    StudentSessionHistoryItem(
                        session = session,
                        record = recordMap[session.id]
                    )
                }.sortedByDescending { it.session.startTime }

                val present = studentRecords.count { it.status == AttendanceRecordEntity.STATUS_PRESENTE }
                val late = studentRecords.count { it.status == AttendanceRecordEntity.STATUS_RETARDO }
                val justified = studentRecords.count { it.status == AttendanceRecordEntity.STATUS_JUSTIFICADO }
                val explicitAbsent = studentRecords.count { it.status == AttendanceRecordEntity.STATUS_AUSENTE }
                val unrecordedAbsent = (gradeSessions.size - studentRecords.size).coerceAtLeast(0)
                val totalAbsent = explicitAbsent + unrecordedAbsent

                StudentDetailedReport(
                    student = student,
                    grade = grade,
                    totalSessions = gradeSessions.size,
                    presentCount = present,
                    lateCount = late,
                    justifiedCount = justified,
                    absentCount = totalAbsent,
                    historyItems = historyItems
                )
            }
        }
    }

    // Sessions
    fun getSessionsForGrade(gradeId: Long): Flow<List<AttendanceSessionEntity>> =
        attendanceDao.getSessionsByGrade(gradeId)

    fun getSession(sessionId: Long): Flow<AttendanceSessionEntity?> =
        attendanceDao.getSessionById(sessionId)

    fun getRecordsForSession(sessionId: Long): Flow<List<AttendanceRecordEntity>> =
        attendanceDao.getRecordsBySession(sessionId)

    fun getStudentsWithAttendanceForSession(
        gradeId: Long,
        sessionId: Long
    ): Flow<List<StudentWithAttendance>> {
        val studentsFlow = studentDao.getStudentsByGrade(gradeId)
        val recordsFlow = attendanceDao.getRecordsBySession(sessionId)
        return combine(studentsFlow, recordsFlow) { students, records ->
            val recordMap = records.associateBy { it.studentId }
            students.map { student ->
                StudentWithAttendance(
                    student = student,
                    record = recordMap[student.id]
                )
            }
        }
    }

    suspend fun startNewSession(
        gradeId: Long,
        topic: String,
        durationMinutes: Int
    ): AttendanceSessionEntity = withContext(Dispatchers.IO) {
        attendanceDao.closeAllActiveSessions()
        val randomNum = (1000..9999).random()
        val sessionCode = "ASIST-$randomNum"
        val session = AttendanceSessionEntity(
            gradeId = gradeId,
            topic = topic.ifBlank { "Clase regular" },
            sessionCode = sessionCode,
            startTime = System.currentTimeMillis(),
            durationMinutes = durationMinutes,
            isActive = true
        )
        val id = attendanceDao.insertSession(session)
        session.copy(id = id)
    }

    suspend fun recordStudentAttendance(
        sessionId: Long,
        studentId: Long,
        gradeId: Long,
        status: String = AttendanceRecordEntity.STATUS_PRESENTE,
        note: String = ""
    ) = withContext(Dispatchers.IO) {
        val existing = attendanceDao.getRecordForStudentInSession(sessionId, studentId)
        if (existing != null) {
            attendanceDao.updateRecord(
                existing.copy(status = status, timestamp = System.currentTimeMillis(), note = note)
            )
        } else {
            attendanceDao.insertRecord(
                AttendanceRecordEntity(
                    sessionId = sessionId,
                    studentId = studentId,
                    gradeId = gradeId,
                    status = status,
                    timestamp = System.currentTimeMillis(),
                    note = note
                )
            )
        }
    }

    suspend fun finishSession(sessionId: Long, gradeId: Long, autoMarkAbsent: Boolean) =
        withContext(Dispatchers.IO) {
            attendanceDao.closeSession(sessionId)
            if (autoMarkAbsent) {
                val enrolledStudents = studentDao.getStudentsByGradeSync(gradeId)
                val existingRecords = attendanceDao.getRecordsBySessionSync(sessionId).map { it.studentId }.toSet()
                val missingRecords = enrolledStudents
                    .filter { !existingRecords.contains(it.id) }
                    .map { student ->
                        AttendanceRecordEntity(
                            sessionId = sessionId,
                            studentId = student.id,
                            gradeId = gradeId,
                            status = AttendanceRecordEntity.STATUS_AUSENTE,
                            timestamp = System.currentTimeMillis(),
                            note = "Ausencia no registrada"
                        )
                    }
                if (missingRecords.isNotEmpty()) {
                    attendanceDao.insertRecords(missingRecords)
                }
            }
        }

    suspend fun recordAttendanceByCode(
        sessionId: Long,
        gradeId: Long,
        studentCode: String
    ): StudentEntity? = withContext(Dispatchers.IO) {
        val student = studentDao.getStudentByCode(studentCode.trim().uppercase())
        if (student != null && student.gradeId == gradeId) {
            recordStudentAttendance(sessionId, student.id, gradeId, AttendanceRecordEntity.STATUS_PRESENTE)
            student
        } else {
            null
        }
    }

    // Teacher Profile & Settings
    suspend fun updateTeacherProfile(profile: TeacherProfileEntity) = withContext(Dispatchers.IO) {
        teacherProfileDao.updateProfile(profile)
    }

    suspend fun ensureDefaultDataLoaded() = withContext(Dispatchers.IO) {
        val existingProfile = teacherProfileDao.getProfileSync()
        if (existingProfile == null) {
            teacherProfileDao.insertProfile(SampleData.defaultTeacher)
        }
        val existingGrades = gradeDao.getAllGrades().firstOrNull()
        if (existingGrades.isNullOrEmpty()) {
            loadDemoData()
        }
    }

    suspend fun getStudentsByGradeSync(gradeId: Long): List<StudentEntity> = withContext(Dispatchers.IO) {
        studentDao.getStudentsByGradeSync(gradeId)
    }

    suspend fun getAllRecordsSync(): List<AttendanceRecordEntity> = withContext(Dispatchers.IO) {
        attendanceDao.getAllRecordsSync()
    }

    suspend fun loadDemoData() = withContext(Dispatchers.IO) {
        clearAllData()
        teacherProfileDao.insertProfile(SampleData.defaultTeacher)
        gradeDao.insertGrades(SampleData.sampleGrades)
        for (grade in SampleData.sampleGrades) {
            studentDao.insertStudents(SampleData.getSampleStudents(grade.id))
        }
        attendanceDao.insertSessions(SampleData.getSampleSessions())
        attendanceDao.insertRecords(SampleData.getSampleRecords())
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        attendanceDao.clearAllRecords()
        attendanceDao.clearAllSessions()
        studentDao.clearAllStudents()
        gradeDao.clearAllGrades()
    }
}
