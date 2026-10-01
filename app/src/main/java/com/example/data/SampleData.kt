package com.example.data

import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.GradeEntity
import com.example.data.model.StudentEntity
import com.example.data.model.TeacherProfileEntity

object SampleData {
    val defaultTeacher = TeacherProfileEntity(
        id = 1,
        name = "Prof. Martín Ramírez",
        school = "Colegio Nacional San Martín",
        subject = "Matemáticas y Ciencias",
        email = "martin.ramirez@colegio.edu",
        defaultSessionDurationMinutes = 15,
        lateToleranceMinutes = 5,
        autoMarkAbsentOnFinish = true
    )

    val sampleGrades = listOf(
        GradeEntity(
            id = 1,
            name = "5° Grado Primaria",
            section = "Grupo A",
            subject = "Matemáticas",
            colorHex = "#1A56DB",
            schoolYear = "2026"
        ),
        GradeEntity(
            id = 2,
            name = "1° Grado Secundaria",
            section = "Grupo B",
            subject = "Ciencias Naturales",
            colorHex = "#0E9F6E",
            schoolYear = "2026"
        ),
        GradeEntity(
            id = 3,
            name = "3° Grado Secundaria",
            section = "Grupo C",
            subject = "Álgebra y Geometría",
            colorHex = "#9061F9",
            schoolYear = "2026"
        )
    )

    fun getSampleStudents(gradeId: Long): List<StudentEntity> {
        return when (gradeId) {
            1L -> listOf(
                StudentEntity(101, 1, "Sofía Valentina Castro", "EST-5A-01", "sofia.castro@est.edu", "555-1011", "#1A56DB"),
                StudentEntity(102, 1, "Mateo Alexander Gómez", "EST-5A-02", "mateo.gomez@est.edu", "555-1012", "#046C4E"),
                StudentEntity(103, 1, "Valentina Morales Ruiz", "EST-5A-03", "valentina.m@est.edu", "555-1013", "#7E3AF2"),
                StudentEntity(104, 1, "Lucas Emiliano Torres", "EST-5A-04", "lucas.t@est.edu", "555-1014", "#C27803"),
                StudentEntity(105, 1, "Camila Daniela Ortiz", "EST-5A-05", "camila.o@est.edu", "555-1015", "#E02424"),
                StudentEntity(106, 1, "Santiago Daniel Herrera", "EST-5A-06", "santiago.h@est.edu", "555-1016", "#0694A2"),
                StudentEntity(107, 1, "Isabella Victoria Flores", "EST-5A-07", "isabella.f@est.edu", "555-1017", "#D61F69"),
                StudentEntity(108, 1, "Sebastián Andrés Mendoza", "EST-5A-08", "sebastian.m@est.edu", "555-1018", "#3F83F8")
            )
            2L -> listOf(
                StudentEntity(201, 2, "Nicolás David Vargas", "EST-1B-01", "nicolas.v@est.edu", "555-2011", "#0E9F6E"),
                StudentEntity(202, 2, "Lucía Mariana Delgado", "EST-1B-02", "lucia.d@est.edu", "555-2012", "#9061F9"),
                StudentEntity(203, 2, "Alejandro José Navarro", "EST-1B-03", "alejandro.n@est.edu", "555-2013", "#1A56DB"),
                StudentEntity(204, 2, "Mariana Elena Silva", "EST-1B-04", "mariana.s@est.edu", "555-2014", "#C27803"),
                StudentEntity(205, 2, "Gabriel Antonio Paredes", "EST-1B-05", "gabriel.p@est.edu", "555-2015", "#0694A2"),
                StudentEntity(206, 2, "Valeria Michelle Rojas", "EST-1B-06", "valeria.r@est.edu", "555-2016", "#E02424")
            )
            3L -> listOf(
                StudentEntity(301, 3, "Carlos Eduardo Medina", "EST-3C-01", "carlos.m@est.edu", "555-3011", "#7E3AF2"),
                StudentEntity(302, 3, "Daniela Fernanda Cruz", "EST-3C-02", "daniela.c@est.edu", "555-3012", "#046C4E"),
                StudentEntity(303, 3, "Diego Emmanuel Reyes", "EST-3C-03", "diego.r@est.edu", "555-3013", "#1A56DB"),
                StudentEntity(304, 3, "Paula Andrea Guzmán", "EST-3C-04", "paula.g@est.edu", "555-3014", "#D61F69"),
                StudentEntity(305, 3, "Fernando Javier Lozano", "EST-3C-05", "fernando.l@est.edu", "555-3015", "#C27803")
            )
            else -> emptyList()
        }
    }

    fun getSampleSessions(): List<AttendanceSessionEntity> {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        return listOf(
            AttendanceSessionEntity(
                id = 1,
                gradeId = 1,
                topic = "Fracciones y Números Decimales",
                sessionCode = "QR-5A-01",
                startTime = now - (oneDay * 2),
                endTime = now - (oneDay * 2) + (45 * 60 * 1000L),
                isActive = false,
                durationMinutes = 15
            ),
            AttendanceSessionEntity(
                id = 2,
                gradeId = 1,
                topic = "Geometría: Perímetros y Áreas",
                sessionCode = "QR-5A-02",
                startTime = now - oneDay,
                endTime = now - oneDay + (45 * 60 * 1000L),
                isActive = false,
                durationMinutes = 15
            ),
            AttendanceSessionEntity(
                id = 3,
                gradeId = 2,
                topic = "Ecosistemas y Biodiversidad",
                sessionCode = "QR-1B-01",
                startTime = now - oneDay,
                endTime = now - oneDay + (40 * 60 * 1000L),
                isActive = false,
                durationMinutes = 15
            )
        )
    }

    fun getSampleRecords(): List<AttendanceRecordEntity> {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        return listOf(
            // Session 1 (Grade 1)
            AttendanceRecordEntity(sessionId = 1, studentId = 101, gradeId = 1, status = AttendanceRecordEntity.STATUS_PRESENTE, timestamp = now - (oneDay * 2) + 120000),
            AttendanceRecordEntity(sessionId = 1, studentId = 102, gradeId = 1, status = AttendanceRecordEntity.STATUS_PRESENTE, timestamp = now - (oneDay * 2) + 180000),
            AttendanceRecordEntity(sessionId = 1, studentId = 103, gradeId = 1, status = AttendanceRecordEntity.STATUS_RETARDO, timestamp = now - (oneDay * 2) + 600000),
            AttendanceRecordEntity(sessionId = 1, studentId = 104, gradeId = 1, status = AttendanceRecordEntity.STATUS_PRESENTE, timestamp = now - (oneDay * 2) + 240000),
            AttendanceRecordEntity(sessionId = 1, studentId = 105, gradeId = 1, status = AttendanceRecordEntity.STATUS_AUSENTE, timestamp = now - (oneDay * 2)),
            AttendanceRecordEntity(sessionId = 1, studentId = 106, gradeId = 1, status = AttendanceRecordEntity.STATUS_PRESENTE, timestamp = now - (oneDay * 2) + 300000),
            AttendanceRecordEntity(sessionId = 1, studentId = 107, gradeId = 1, status = AttendanceRecordEntity.STATUS_PRESENTE, timestamp = now - (oneDay * 2) + 150000),
            AttendanceRecordEntity(sessionId = 1, studentId = 108, gradeId = 1, status = AttendanceRecordEntity.STATUS_JUSTIFICADO, timestamp = now - (oneDay * 2), note = "Cita médica"),

            // Session 2 (Grade 1)
            AttendanceRecordEntity(sessionId = 2, studentId = 101, gradeId = 1, status = AttendanceRecordEntity.STATUS_PRESENTE, timestamp = now - oneDay + 100000),
            AttendanceRecordEntity(sessionId = 2, studentId = 102, gradeId = 1, status = AttendanceRecordEntity.STATUS_PRESENTE, timestamp = now - oneDay + 110000),
            AttendanceRecordEntity(sessionId = 2, studentId = 103, gradeId = 1, status = AttendanceRecordEntity.STATUS_PRESENTE, timestamp = now - oneDay + 150000),
            AttendanceRecordEntity(sessionId = 2, studentId = 104, gradeId = 1, status = AttendanceRecordEntity.STATUS_RETARDO, timestamp = now - oneDay + 500000),
            AttendanceRecordEntity(sessionId = 2, studentId = 105, gradeId = 1, status = AttendanceRecordEntity.STATUS_PRESENTE, timestamp = now - oneDay + 200000),
            AttendanceRecordEntity(sessionId = 2, studentId = 106, gradeId = 1, status = AttendanceRecordEntity.STATUS_PRESENTE, timestamp = now - oneDay + 130000),
            AttendanceRecordEntity(sessionId = 2, studentId = 107, gradeId = 1, status = AttendanceRecordEntity.STATUS_PRESENTE, timestamp = now - oneDay + 180000),
            AttendanceRecordEntity(sessionId = 2, studentId = 108, gradeId = 1, status = AttendanceRecordEntity.STATUS_PRESENTE, timestamp = now - oneDay + 140000)
        )
    }
}
