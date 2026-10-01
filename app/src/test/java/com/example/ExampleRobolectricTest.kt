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
    }
}
