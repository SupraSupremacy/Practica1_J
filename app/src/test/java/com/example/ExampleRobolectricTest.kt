package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.GradeEntity
import com.example.data.model.StudentEntity
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Asistencia QR", appName)
    }

    @Test
    fun testQrCodeGeneration() {
        val bitmap = QrCodeGenerator.generateQrBitmap("TEST_QR_SESSION_123", sizePx = 200)
        assertNotNull("El bitmap QR generado no debe ser nulo", bitmap)
        assertEquals(200, bitmap?.width)
        assertEquals(200, bitmap?.height)
    }

    @Test
    fun testDatabaseOperations() = runBlocking {
        val gradeDao = database.gradeDao()
        val studentDao = database.studentDao()
        val attendanceDao = database.attendanceDao()

        // Insert Grade
        val grade = GradeEntity(
            id = 1,
            name = "5° Primaria",
            section = "Grupo A",
            subject = "Matemáticas"
        )
        gradeDao.insertGrade(grade)

        val grades = gradeDao.getAllGrades().first()
        assertEquals(1, grades.size)
        assertEquals("5° Primaria - Grupo A", grades[0].displayName)

        // Insert Student
        val student = StudentEntity(
            id = 101,
            gradeId = 1,
            fullName = "Sofía Castro",
            studentCode = "EST-01"
        )
        studentDao.insertStudent(student)

        val students = studentDao.getStudentsByGrade(1).first()
        assertEquals(1, students.size)
        assertEquals("Sofía Castro", students[0].fullName)

        // Insert Session & Record
        val session = AttendanceSessionEntity(
            id = 1,
            gradeId = 1,
            topic = "Fracciones",
            sessionCode = "QR-999"
        )
        attendanceDao.insertSession(session)

        val record = AttendanceRecordEntity(
            sessionId = 1,
            studentId = 101,
            gradeId = 1,
            status = AttendanceRecordEntity.STATUS_PRESENTE
        )
        attendanceDao.insertRecord(record)

        val records = attendanceDao.getRecordsBySession(1).first()
        assertEquals(1, records.size)
        assertEquals(AttendanceRecordEntity.STATUS_PRESENTE, records[0].status)

        // Test student search by name and code
        val searchByName = studentDao.searchStudents("Sofía").first()
        assertEquals(1, searchByName.size)
        assertEquals("Sofía Castro", searchByName[0].fullName)

        val searchByCode = studentDao.searchStudents("EST-01").first()
        assertEquals(1, searchByCode.size)
        assertEquals("EST-01", searchByCode[0].studentCode)

        // Test updating attendance record with reason/note
        val existingRecord = records[0]
        attendanceDao.updateRecord(
            existingRecord.copy(
                status = AttendanceRecordEntity.STATUS_JUSTIFICADO,
                note = "Presentó receta médica certificada"
            )
        )
        val updatedRecords = attendanceDao.getRecordsBySession(1).first()
        assertEquals(AttendanceRecordEntity.STATUS_JUSTIFICADO, updatedRecords[0].status)
        assertEquals("Presentó receta médica certificada", updatedRecords[0].note)
    }

    @Test
    fun testAttendancePercentageCalculation() {
        val student = StudentEntity(id = 1, gradeId = 1, fullName = "Test Student", studentCode = "T-01")

        // 4 sessions: 1 present, 1 late, 1 justified, 1 absent -> (1+1+1)/4 = 3/4 = 75%
        val summary1 = com.example.data.model.StudentAttendanceSummary(
            student = student,
            totalSessions = 4,
            presentCount = 1,
            lateCount = 1,
            justifiedCount = 1,
            absentCount = 1
        )
        assertEquals(75, summary1.attendancePercentage)

        // Only absent does not count: 2 sessions: 1 late, 1 justified -> 2/2 = 100%
        val summary2 = com.example.data.model.StudentAttendanceSummary(
            student = student,
            totalSessions = 2,
            presentCount = 0,
            lateCount = 1,
            justifiedCount = 1,
            absentCount = 0
        )
        assertEquals(100, summary2.attendancePercentage)

        // 3 sessions: 3 absent -> 0%
        val summary3 = com.example.data.model.StudentAttendanceSummary(
            student = student,
            totalSessions = 3,
            presentCount = 0,
            lateCount = 0,
            justifiedCount = 0,
            absentCount = 3
        )
        assertEquals(0, summary3.attendancePercentage)
    }
}
