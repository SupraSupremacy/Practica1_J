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

    fun exportStudentReport(
        context: Context,
        report: com.example.data.model.StudentDetailedReport
    ) {
        val student = report.student
        val grade = report.grade
        val sb = StringBuilder()
        sb.append("REPORTE INDIVIDUAL DE ASISTENCIA\n")
        sb.append("Estudiante;${student.fullName}\n")
        sb.append("Matricula;${student.studentCode}\n")
        sb.append("Grado;${grade?.displayName ?: "No asignado"}\n")
        sb.append("Materia;${grade?.subject ?: "-"}\n")
        sb.append("Total de Clases;${report.totalSessions}\n")
        sb.append("Asistencias (Presente);${report.presentCount}\n")
        sb.append("Retardos;${report.lateCount}\n")
        sb.append("Justificados;${report.justifiedCount}\n")
        sb.append("Inasistencias (Ausente);${report.absentCount}\n")
        sb.append("Porcentaje de Asistencia;${report.attendancePercentage}%\n\n")

        sb.append("DETALLE DE CLASES\n")
        sb.append("Fecha;Hora;Tema;Estado;Nota\n")
        for (item in report.historyItems) {
            val dateStr = Formatters.formatDate(item.session.startTime)
            val timeStr = if (item.record != null) Formatters.formatTime(item.record.timestamp) else "-"
            val status = item.record?.status ?: "AUSENTE"
            val note = item.record?.note ?: ""
            sb.append("$dateStr;$timeStr;${item.session.topic};$status;\"$note\"\n")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de Asistencia - ${student.fullName} (${student.studentCode})")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        val chooser = Intent.createChooser(shareIntent, "Compartir Reporte de Estudiante")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun exportStudentAbsencesExcel(
        context: Context,
        report: com.example.data.model.StudentDetailedReport,
        teacherName: String
    ) {
        val student = report.student
        val grade = report.grade
        val absences = report.historyItems.filter {
            it.record == null || it.record.status == AttendanceRecordEntity.STATUS_AUSENTE
        }

        val sb = StringBuilder()
        // UTF-8 BOM so Excel automatically handles accents and symbols cleanly
        sb.append("\uFEFF")
        // Excel CSV separator directive
        sb.append("sep=;\n")
        sb.append("REPORTE DE INASISTENCIAS DEL ESTUDIANTE\n")
        sb.append("Estudiante;${student.fullName}\n")
        sb.append("Matrícula;${student.studentCode}\n")
        sb.append("Grado / Grupo;${grade?.displayName ?: "No asignado"}\n")
        sb.append("Materia;${grade?.subject ?: "-"}\n")
        sb.append("Total de Clases Registradas;${report.totalSessions}\n")
        sb.append("Total de Inasistencias;${absences.size}\n")
        sb.append("Porcentaje Global de Asistencia;${report.attendancePercentage}%\n\n")

        // Mandatory columns requested:
        // Nombre de la sesión;Fecha y hora de la sesión;Docente con el que fué dicha sesión
        sb.append("Nombre de la sesión;Fecha y hora de la sesión;Docente con el que fué dicha sesión;Motivo / Nota\n")

        if (absences.isEmpty()) {
            sb.append("El alumno no presenta inasistencias registradas (100% Asistencia);-;$teacherName;Sin registros de falta\n")
        } else {
            for (item in absences) {
                val sessionTopic = item.session.topic
                val dateTimeStr = "${Formatters.formatDate(item.session.startTime)} ${Formatters.formatTime(item.session.startTime)}"
                val note = item.record?.note?.ifBlank { "Inasistencia registrada" } ?: "Inasistencia registrada"
                sb.append("\"$sessionTopic\";\"$dateTimeStr\";\"$teacherName\";\"$note\"\n")
            }
        }

        try {
            val safeStudentCode = student.studentCode.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val fileName = "Inasistencias_${safeStudentCode}.csv"
            val file = java.io.File(context.cacheDir, fileName)
            file.writeText(sb.toString(), Charsets.UTF_8)

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Reporte de Inasistencias - ${student.fullName}")
                putExtra(Intent.EXTRA_TEXT, sb.toString())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(viewIntent, "Abrir reporte de inasistencias en Excel").apply {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(shareIntent))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Reporte de Inasistencias - ${student.fullName}")
                putExtra(Intent.EXTRA_TEXT, sb.toString())
            }
            val chooser = Intent.createChooser(shareIntent, "Abrir / Compartir Reporte de Inasistencias")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }
}
