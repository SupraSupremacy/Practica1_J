package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.GradeEntity
import com.example.data.model.StudentEntity
import com.example.data.model.StudentWithAttendance
import com.example.ui.AttendanceViewModel
import com.example.ui.components.FullScreenQrDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusLateAmber
import com.example.ui.theme.StatusPresentGreen
import com.example.util.Formatters
import com.example.util.QrCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateQrScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val activeGrade by viewModel.activeSessionGrade.collectAsState()
    val activeStudents by viewModel.activeSessionStudents.collectAsState()
    val allGrades by viewModel.allGrades.collectAsState()
    val teacherProfile by viewModel.teacherProfile.collectAsState()
    val timerSeconds by viewModel.activeSessionTimerSeconds.collectAsState()
    val isQrFullScreen by viewModel.isQrFullScreen.collectAsState()
    val lastScannedStudent by viewModel.lastScannedStudent.collectAsState()

    var showEndSessionConfirm by remember { mutableStateOf(false) }

    // Full screen projector modal
    if (isQrFullScreen && activeSession != null) {
        val presentCount = activeStudents.count {
            it.record?.status == AttendanceRecordEntity.STATUS_PRESENTE ||
            it.record?.status == AttendanceRecordEntity.STATUS_RETARDO
        }
        FullScreenQrDialog(
            session = activeSession!!,
            grade = activeGrade,
            presentCount = presentCount,
            totalCount = activeStudents.size,
            onDismiss = { viewModel.setQrFullScreen(false) }
        )
    }

    if (showEndSessionConfirm && activeSession != null) {
        AlertDialog(
            onDismissRequest = { showEndSessionConfirm = false },
            title = { Text("¿Finalizar sesión de asistencia?") },
            text = {
                Text("Al finalizar, se guardará el registro permanente y los estudiantes no marcados se registrarán como ausentes según tus ajustes.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEndSessionConfirm = false
                        viewModel.finishActiveSession()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_finish_session_btn")
                ) {
                    Text("Finalizar y Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndSessionConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (activeSession != null) {
            ActiveSessionView(
                session = activeSession!!,
                grade = activeGrade,
                students = activeStudents,
                timerSeconds = timerSeconds,
                lastScannedStudent = lastScannedStudent,
                onProjectorClick = { viewModel.setQrFullScreen(true) },
                onFinishClick = { showEndSessionConfirm = true },
                onRecordStudent = { studentId, status -> viewModel.recordAttendance(studentId, status) },
                onSimulateScan = { student -> viewModel.simulateStudentScan(student) },
                onCheckInCode = { code -> viewModel.checkInByCode(code) },
                onMarkRemainingAbsent = { viewModel.markRemainingAbsent() },
                onOpenBadge = { student -> viewModel.openStudentBadge(student) }
            )
        } else {
            NewSessionSetupView(
                grades = allGrades,
                teacherName = teacherProfile?.name ?: "Docente",
                schoolName = teacherProfile?.school ?: "Institución Educativa",
                defaultDuration = teacherProfile?.defaultSessionDurationMinutes ?: 15,
                onStartSession = { gradeId, topic, duration ->
                    viewModel.startQrSession(gradeId, topic, duration)
                },
                onGoToGradesTab = { viewModel.switchTab(1) }
            )
        }
    }
}

@Composable
private fun NewSessionSetupView(
    grades: List<GradeEntity>,
    teacherName: String,
    schoolName: String,
    defaultDuration: Int,
    onStartSession: (gradeId: Long, topic: String, durationMinutes: Int) -> Unit,
    onGoToGradesTab: () -> Unit
) {
    var selectedGradeId by remember(grades) { mutableStateOf(grades.firstOrNull()?.id ?: 0L) }
    var topicText by remember { mutableStateOf("") }
    var durationMinutes by remember(defaultDuration) { mutableIntStateOf(defaultDuration) }

    val currentGrade = grades.find { it.id == selectedGradeId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Teacher Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_welcome_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Bienvenido, $teacherName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = schoolName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Setup Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.QrCode2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Iniciar Sesión de Asistencia",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Genera un código QR dinámico para que los alumnos tomen asistencia en vivo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (grades.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Aún no tienes grados registrados.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onGoToGradesTab,
                                    modifier = Modifier.testTag("go_to_add_grades_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Crear mi primer grado")
                                }
                            }
                        }
                    } else {
                        // Grade Selector
                        Text(
                            text = "Selecciona el Grado / Grupo",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(grades) { grade ->
                                val isSelected = grade.id == selectedGradeId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedGradeId = grade.id
                                        if (topicText.isBlank()) {
                                            topicText = grade.subject
                                        }
                                    },
                                    label = { Text(grade.displayName) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null) }
                                    } else null,
                                    modifier = Modifier.testTag("grade_chip_${grade.id}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Topic TextField
                        OutlinedTextField(
                            value = topicText,
                            onValueChange = { topicText = it },
                            label = { Text("Tema o Lección de la clase") },
                            placeholder = { Text(currentGrade?.subject ?: "Ej. Ecuaciones Lineales") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("session_topic_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Duration Picker
                        Text(
                            text = "Duración sugerida del pase de lista",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15, 30).forEach { mins ->
                                val isSelected = durationMinutes == mins
                                OutlinedButton(
                                    onClick = { durationMinutes = mins },
                                    modifier = Modifier.weight(1f),
                                    colors = if (isSelected) {
                                        ButtonDefaults.outlinedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    } else {
                                        ButtonDefaults.outlinedButtonColors()
                                    }
                                ) {
                                    Text("${mins}m")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Start Button
                        Button(
                            onClick = {
                                val topic = topicText.ifBlank { currentGrade?.subject ?: "Pase de Lista" }
                                onStartSession(selectedGradeId, topic, durationMinutes)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_qr_session_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generar Código QR de Clase",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Instructions preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "¿Cómo funciona?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Muestra el código QR proyectado en la pizarra o pantalla del aula.\n" +
                               "2. Los alumnos apuntan la cámara de su dispositivo al código QR.\n" +
                               "3. El tablero se actualiza en tiempo real mostrando quiénes ya llegaron.\n" +
                               "4. También puedes usar el escáner rápido o marcar ausentes al terminar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveSessionView(
    session: com.example.data.model.AttendanceSessionEntity,
    grade: GradeEntity?,
    students: List<StudentWithAttendance>,
    timerSeconds: Long,
    lastScannedStudent: StudentEntity?,
    onProjectorClick: () -> Unit,
    onFinishClick: () -> Unit,
    onRecordStudent: (studentId: Long, status: String) -> Unit,
    onSimulateScan: (StudentEntity) -> Unit,
    onCheckInCode: (String) -> Unit,
    onMarkRemainingAbsent: () -> Unit,
    onOpenBadge: (StudentEntity) -> Unit
) {
    var studentCodeInput by remember { mutableStateOf("") }
    var filterText by remember { mutableStateOf("") }

    val qrPayload = "SESSION_ID:${session.id}|CODE:${session.sessionCode}|GRADE:${session.gradeId}|TOPIC:${session.topic}"
    val qrBitmap = remember(session.id, session.sessionCode) {
        QrCodeGenerator.generateQrBitmap(qrPayload, sizePx = 500)
    }

    val presentCount = students.count { it.record?.status == AttendanceRecordEntity.STATUS_PRESENTE }
    val lateCount = students.count { it.record?.status == AttendanceRecordEntity.STATUS_RETARDO }
    val absentCount = students.count { it.record?.status == AttendanceRecordEntity.STATUS_AUSENTE }
    val pendingCount = students.count { it.record == null }
    val totalCount = students.size

    val progress = if (totalCount > 0) (presentCount + lateCount).toFloat() / totalCount else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Celebration Toast/Banner when student is scanned
        item {
            AnimatedVisibility(
                visible = lastScannedStudent != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (lastScannedStudent != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_success_banner"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "¡Asistencia registrada para ${lastScannedStudent.fullName}!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Active Session Header & Timer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_session_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(StatusPresentGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "EN VIVO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPresentGreen
                                )
                            }
                            Text(
                                text = grade?.displayName ?: "Clase Activa",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = session.topic,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Time elapsed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Formatters.formatDuration(timerSeconds),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // QR Display & Projector Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // QR image
                        if (qrBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .padding(6.dp)
                                    .clickable { onProjectorClick() }
                                    .testTag("active_qr_code_image"),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Código QR de asistencia",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Código Manual:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = session.sessionCode,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = onProjectorClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("projector_mode_btn")
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Proyector / Zoom", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = onFinishClick,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("finish_session_btn")
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Finalizar Clase", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Scoreboard Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Asistencia: ${presentCount + lateCount} de $totalCount (${(progress * 100).toInt()}%)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        if (pendingCount > 0) {
                            TextButton(
                                onClick = onMarkRemainingAbsent,
                                modifier = Modifier.testTag("mark_remaining_absent_btn")
                            ) {
                                Text("Marcar pendientes ausentes", fontSize = 11.sp, color = StatusAbsentRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ScoreStat(title = "Presentes", count = presentCount, color = StatusPresentGreen)
                        ScoreStat(title = "Retardos", count = lateCount, color = StatusLateAmber)
                        ScoreStat(title = "Ausentes", count = absentCount, color = StatusAbsentRed)
                        ScoreStat(title = "Pendientes", count = pendingCount, color = Color.Gray)
                    }
                }
            }
        }

        // Quick Code Check-in Input
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = studentCodeInput,
                    onValueChange = { studentCodeInput = it },
                    placeholder = { Text("Ingresar matrícula o código...", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_code_input")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (studentCodeInput.isNotBlank()) {
                            onCheckInCode(studentCodeInput)
                            studentCodeInput = ""
                        }
                    },
                    modifier = Modifier.testTag("quick_code_submit_btn")
                ) {
                    Text("Marcar")
                }
            }
        }

        // Student Roster Header & Search
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lista de Estudiantes (${students.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toca para registrar o ver carnet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Student items
        items(students, key = { it.student.id }) { item ->
            ActiveStudentItem(
                item = item,
                onRecord = { status -> onRecordStudent(item.student.id, status) },
                onSimulateScan = { onSimulateScan(item.student) },
                onOpenBadge = { onOpenBadge(item.student) }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun ScoreStat(title: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$count", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = color)
        Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActiveStudentItem(
    item: StudentWithAttendance,
    onRecord: (String) -> Unit,
    onSimulateScan: () -> Unit,
    onOpenBadge: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val student = item.student
    val record = item.record

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_item_${student.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initial Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(student.avatarColorHex))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.fullName.firstOrNull()?.toString()?.uppercase() ?: "E",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = student.studentCode,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status Badge
            StatusBadge(status = record?.status, compact = true)

            Spacer(modifier = Modifier.width(4.dp))

            // Action Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("student_menu_btn_${student.id}")
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Simular escaneo de QR") },
                        onClick = {
                            showMenu = false
                            onSimulateScan()
                        },
                        leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Marcar Presente") },
                        onClick = {
                            showMenu = false
                            onRecord(AttendanceRecordEntity.STATUS_PRESENTE)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Marcar Retardo") },
                        onClick = {
                            showMenu = false
                            onRecord(AttendanceRecordEntity.STATUS_RETARDO)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Marcar Justificado") },
                        onClick = {
                            showMenu = false
                            onRecord(AttendanceRecordEntity.STATUS_JUSTIFICADO)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Marcar Ausente") },
                        onClick = {
                            showMenu = false
                            onRecord(AttendanceRecordEntity.STATUS_AUSENTE)
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Ver Carnet QR") },
                        onClick = {
                            showMenu = false
                            onOpenBadge()
                        }
                    )
                }
            }
        }
    }
}
