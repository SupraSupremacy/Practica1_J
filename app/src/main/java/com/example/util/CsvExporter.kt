package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.GradeEntity
import com.example.data.model.StudentEntity

object CsvExporter {
    fun exportSessionToCsv(
        context: Context,
        grade: GradeEntity,
        session: AttendanceSessionEntity,
        students: List<StudentEntity>,
        records: List<AttendanceRecordEntity>
    ) {
        val recordMap = records.associateBy { it.studentId }
        val sb = StringBuilder()
        sb.append("Grado;Grupo;Materia;Fecha;Tema;Matricula;Estudiante;Estado;Hora;Nota\n")

        val dateStr = Formatters.formatDate(session.startTime)
        for (student in students) {
            val rec = recordMap[student.id]
            val status = rec?.status ?: "AUSENTE"
            val time = if (rec != null) Formatters.formatTime(rec.timestamp) else "-"
            val note = rec?.note ?: ""

            sb.append("${grade.name};${grade.section};${grade.subject};$dateStr;${session.topic};${student.studentCode};\"${student.fullName}\";$status;$time;\"$note\"\n")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Asistencia ${grade.name} - ${session.topic} ($dateStr)")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        val chooser = Intent.createChooser(shareIntent, "Compartir reporte de asistencia (CSV)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun exportOverallReport(
        context: Context,
        grades: List<GradeEntity>,
        allStudents: List<StudentEntity>,
        allRecords: List<AttendanceRecordEntity>,
        allSessions: List<AttendanceSessionEntity>
    ) {
        val gradeMap = grades.associateBy { it.id }
        val sessionMap = allSessions.associateBy { it.id }
        val sb = StringBuilder()
        sb.append("Grado;Grupo;Materia;Fecha;Tema;Matricula;Estudiante;Estado\n")

        for (rec in allRecords) {
            val student = allStudents.find { it.id == rec.studentId }
            val grade = gradeMap[rec.gradeId]
            val session = sessionMap[rec.sessionId]
            val dateStr = if (session != null) Formatters.formatDate(session.startTime) else "-"
            val topic = session?.topic ?: "Clase"

            if (student != null && grade != null) {
                sb.append("${grade.name};${grade.section};${grade.subject};$dateStr;$topic;${student.studentCode};\"${student.fullName}\";${rec.status}\n")
            }
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Reporte General de Asistencias - Docente")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        val chooser = Intent.createChooser(shareIntent, "Exportar Reporte Completo (CSV)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
