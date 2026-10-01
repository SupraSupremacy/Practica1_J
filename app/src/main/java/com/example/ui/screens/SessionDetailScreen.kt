package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.GradeEntity
import com.example.data.model.StudentWithAttendance
import com.example.ui.AttendanceViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusJustifiedBlue
import com.example.ui.theme.StatusLateAmber
import com.example.ui.theme.StatusPresentGreen
import com.example.util.CsvExporter
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    session: AttendanceSessionEntity,
    grade: GradeEntity?,
    viewModel: AttendanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val studentsWithAttendance by viewModel.selectedSessionStudents.collectAsState()

    var studentToEdit by remember { mutableStateOf<StudentWithAttendance?>(null) }

    if (studentToEdit != null) {
        EditAttendanceDialog(
            item = studentToEdit!!,
            session = session,
            onDismiss = { studentToEdit = null },
            onSave = { newStatus, note ->
                val s = studentToEdit
                studentToEdit = null
                if (s != null) {
                    viewModel.updateSessionRecord(
                        sessionId = session.id,
                        studentId = s.student.id,
                        gradeId = session.gradeId,
                        status = newStatus,
                        note = note
                    )
                }
            }
        )
    }

    val presentCount = studentsWithAttendance.count { it.record?.status == AttendanceRecordEntity.STATUS_PRESENTE }
    val lateCount = studentsWithAttendance.count { it.record?.status == AttendanceRecordEntity.STATUS_RETARDO }
    val justifiedCount = studentsWithAttendance.count { it.record?.status == AttendanceRecordEntity.STATUS_JUSTIFICADO }
    val absentCount = studentsWithAttendance.count {
        it.record?.status == AttendanceRecordEntity.STATUS_AUSENTE || it.record == null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = session.topic,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${grade?.displayName ?: ""} • ${Formatters.formatDate(session.startTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("session_detail_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (grade != null) {
                                val students = studentsWithAttendance.map { it.student }
                                val records = studentsWithAttendance.mapNotNull { it.record }
                                CsvExporter.exportSessionToCsv(context, grade, session, students, records)
                            }
                        },
                        modifier = Modifier.testTag("export_session_csv_btn")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Exportar CSV",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            // Session Stats Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resumen de la Clase",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$presentCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StatusPresentGreen)
                                Text("Presentes", fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$lateCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StatusLateAmber)
                                Text("Retardos", fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$justifiedCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StatusJustifiedBlue)
                                Text("Justificados", fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$absentCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StatusAbsentRed)
                                Text("Ausentes", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Registro de Asistencia (${studentsWithAttendance.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Toca para modificar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(studentsWithAttendance, key = { it.student.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { studentToEdit = item }
                        .testTag("session_record_${item.student.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(item.student.avatarColorHex))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.student.fullName.firstOrNull()?.uppercase() ?: "E",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.student.fullName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = item.student.studentCode,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(status = item.record?.status ?: AttendanceRecordEntity.STATUS_AUSENTE, compact = true)
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { studentToEdit = item },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("edit_attendance_btn_${item.student.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar asistencia",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Prominent Motivo / Justificación banner if recorded
                        if (!item.record?.note.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Motivo: ${item.record?.note}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Toca para modificar asistencia o registrar motivo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun EditAttendanceDialog(
    item: StudentWithAttendance,
    session: AttendanceSessionEntity,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var selectedStatus by remember {
        mutableStateOf(item.record?.status ?: AttendanceRecordEntity.STATUS_AUSENTE)
    }
    var reason by remember { mutableStateOf(item.record?.note ?: "") }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val options = listOf(
        AttendanceRecordEntity.STATUS_PRESENTE to "Presente",
        AttendanceRecordEntity.STATUS_RETARDO to "Retardo",
        AttendanceRecordEntity.STATUS_JUSTIFICADO to "Justificado",
        AttendanceRecordEntity.STATUS_AUSENTE to "Ausente"
    )

    val quickReasons = listOf(
        "Justificante médico",
        "Llegó tarde con permiso",
        "Permiso de dirección",
        "Corrección de pase de lista",
        "Actividad escolar"
    )

    val isReasonValid = reason.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Modificar Asistencia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header context
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = item.student.fullName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${item.student.studentCode} • ${session.topic} (${Formatters.formatDate(session.startTime)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Selecciona el nuevo estado:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Status selectors
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.forEach { (statusKey, label) ->
                        val isSelected = selectedStatus == statusKey
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStatus = statusKey }
                                .testTag("edit_session_dialog_select_$statusKey"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                StatusBadge(status = statusKey, compact = true)
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Reason Field (MANDATORY)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Motivo de la modificación",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "* Requerido",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Por favor explica el por qué se editó dicha asistencia para registrar la justificación.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Escribe el motivo del cambio *") },
                    placeholder = { Text("Ej. Presentó justificante médico...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_attendance_edit_reason_input"),
                    minLines = 2,
                    isError = hasAttemptedSubmit && !isReasonValid,
                    supportingText = {
                        if (hasAttemptedSubmit && !isReasonValid) {
                            Text(
                                "Se requiere dejar un mensaje del por qué se editó la asistencia.",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Campo obligatorio requerido para guardar.")
                        }
                    }
                )

                // Quick suggestions chips
                Text(
                    text = "Sugerencias rápidas de motivo:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickReasons) { suggestion ->
                        SuggestionChip(
                            onClick = {
                                reason = if (reason.isBlank()) suggestion else "$reason - $suggestion"
                            },
                            label = { Text(suggestion, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    if (isReasonValid) {
                        onSave(selectedStatus, reason.trim())
                    }
                },
                enabled = isReasonValid,
                modifier = Modifier.testTag("save_attendance_edit_btn")
            ) {
                Text("Guardar Cambio")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
