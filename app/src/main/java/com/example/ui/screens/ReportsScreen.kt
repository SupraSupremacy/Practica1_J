package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.StudentDetailedReport
import com.example.data.model.StudentSessionHistoryItem
import com.example.ui.AttendanceViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.components.StudentBadgeDialog
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusJustifiedBg
import com.example.ui.theme.StatusJustifiedBlue
import com.example.ui.theme.StatusLateAmber
import com.example.ui.theme.StatusLateBg
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusPresentGreen
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val allReports by viewModel.allStudentReports.collectAsState()
    val allGrades by viewModel.allGrades.collectAsState()
    val selectedReportStudentId by viewModel.selectedReportStudentId.collectAsState()
    val selectedStudentReport by viewModel.selectedReportStudent.collectAsState()
    val studentForBadge by viewModel.selectedStudentForBadge.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedGradeFilterId by remember { mutableStateOf<Long?>(null) } // null = All grades

    // Student QR Badge Dialog
    if (studentForBadge != null) {
        StudentBadgeDialog(
            student = studentForBadge!!,
            gradeName = selectedStudentReport?.grade?.displayName ?: "",
            onDismiss = { viewModel.openStudentBadge(null) }
        )
    }

    if (selectedStudentReport != null) {
        // Detailed inspection view of a single student
        StudentReportDetailView(
            report = selectedStudentReport!!,
            onBack = { viewModel.selectReportStudent(null) },
            onOpenBadge = { viewModel.openStudentBadge(selectedStudentReport!!.student) },
            onExportReport = { context ->
                viewModel.exportStudentReport(context, selectedStudentReport!!)
            },
            onUpdateRecord = { sessionId, studentId, gradeId, status, note ->
                viewModel.updateSessionRecord(sessionId, studentId, gradeId, status, note)
            }
        )
    } else {
        // List & Search View
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Reportes de Asistencia",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Consulta asistencias e inasistencias por estudiante",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("report_search_input"),
                    placeholder = { Text("Buscar por nombre o código / matrícula...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                // Grade Filter Chips
                if (allGrades.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedGradeFilterId == null,
                                onClick = { selectedGradeFilterId = null },
                                label = { Text("Todos los grados") },
                                modifier = Modifier.testTag("filter_all_grades")
                            )
                        }
                        items(allGrades) { grade ->
                            FilterChip(
                                selected = selectedGradeFilterId == grade.id,
                                onClick = { selectedGradeFilterId = grade.id },
                                label = { Text(grade.displayName) },
                                modifier = Modifier.testTag("filter_grade_${grade.id}")
                            )
                        }
                    }
                }

                // Filtered List
                val filteredReports = remember(allReports, searchQuery, selectedGradeFilterId) {
                    allReports.filter { report ->
                        val matchesGrade = selectedGradeFilterId == null || report.student.gradeId == selectedGradeFilterId
                        val matchesQuery = searchQuery.isBlank() ||
                                report.student.fullName.contains(searchQuery, ignoreCase = true) ||
                                report.student.studentCode.contains(searchQuery, ignoreCase = true)
                        matchesGrade && matchesQuery
                    }
                }

                // Stats overview chip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mostrando ${filteredReports.size} de ${allReports.size} alumnos",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (filteredReports.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No se encontraron estudiantes con '$searchQuery'" else "No hay estudiantes registrados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Intenta buscar con otro nombre o matrícula.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredReports, key = { it.student.id }) { report ->
                            StudentReportCard(
                                report = report,
                                onClick = { viewModel.selectReportStudent(report.student.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentReportCard(
    report: StudentDetailedReport,
    onClick: () -> Unit
) {
    val student = report.student
    val grade = report.grade
    val rate = report.attendancePercentage

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("report_student_card_${student.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top row: Avatar + Name + Grade badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initial Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(student.avatarColorHex))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.fullName.firstOrNull()?.uppercase() ?: "E",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = student.studentCode,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (grade != null) {
                            Text(
                                text = " • ${grade.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Attendance rate pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (rate >= 80) StatusPresentBg else StatusAbsentBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$rate%",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = if (rate >= 80) StatusPresentGreen else StatusAbsentRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Linear Progress Indicator
            LinearProgressIndicator(
                progress = { (rate / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (rate >= 80) StatusPresentGreen else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Row: Asistencias, Inasistencias, Retardos, Justificados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReportMetricItem(
                    label = "Asistencias",
                    count = report.presentCount,
                    color = StatusPresentGreen
                )
                ReportMetricItem(
                    label = "Inasistencias",
                    count = report.absentCount,
                    color = StatusAbsentRed
                )
                ReportMetricItem(
                    label = "Retardos",
                    count = report.lateCount,
                    color = StatusLateAmber
                )
                ReportMetricItem(
                    label = "Justificados",
                    count = report.justifiedCount,
                    color = StatusJustifiedBlue
                )
            }
        }
    }
}

@Composable
private fun ReportMetricItem(
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentReportDetailView(
    report: StudentDetailedReport,
    onBack: () -> Unit,
    onOpenBadge: () -> Unit,
    onExportReport: (android.content.Context) -> Unit,
    onUpdateRecord: (sessionId: Long, studentId: Long, gradeId: Long, status: String, note: String) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val student = report.student
    val grade = report.grade
    val rate = report.attendancePercentage
    var itemToEdit by remember { mutableStateOf<StudentSessionHistoryItem?>(null) }

    if (itemToEdit != null) {
        EditReportAttendanceDialog(
            item = itemToEdit!!,
            studentName = student.fullName,
            onDismiss = { itemToEdit = null },
            onSave = { newStatus, reason ->
                val itm = itemToEdit
                itemToEdit = null
                if (itm != null) {
                    onUpdateRecord(
                        itm.session.id,
                        student.id,
                        student.gradeId,
                        newStatus,
                        reason
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = student.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Reporte Detallado • ${student.studentCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("report_detail_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenBadge,
                        modifier = Modifier.testTag("report_open_badge_btn")
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = "Carnet QR", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { onExportReport(context) },
                        modifier = Modifier.testTag("report_share_csv_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir reporte", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Student Summary Card
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
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(student.avatarColorHex))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.fullName.firstOrNull()?.uppercase() ?: "E",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.fullName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Matrícula: ${student.studentCode}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${grade?.displayName ?: "Sin Grado"} (${grade?.subject ?: ""})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Contact info if present
                        if (student.email.isNotBlank() || student.phone.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            if (student.email.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(student.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (student.phone.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(student.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions row: Carnet QR & Export
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenBadge,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Carnet QR")
                            }

                            Button(
                                onClick = { onExportReport(context) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Compartir")
                            }
                        }
                    }
                }
            }

            // Stat Cards Grid
            item {
                Text(
                    text = "Métricas de Asistencia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatBox(
                        title = "Asistencias",
                        value = "${report.presentCount}",
                        subtitle = "Clases presente",
                        icon = Icons.Default.CheckCircle,
                        color = StatusPresentGreen,
                        bgColor = StatusPresentBg,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Inasistencias",
                        value = "${report.absentCount}",
                        subtitle = "Ausencias",
                        icon = Icons.Outlined.Cancel,
                        color = StatusAbsentRed,
                        bgColor = StatusAbsentBg,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatBox(
                        title = "Retardos",
                        value = "${report.lateCount}",
                        subtitle = "Llegadas tarde",
                        icon = Icons.Default.Schedule,
                        color = StatusLateAmber,
                        bgColor = StatusLateBg,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Justificados",
                        value = "${report.justifiedCount}",
                        subtitle = "Permisos válidos",
                        icon = Icons.Default.Help,
                        color = StatusJustifiedBlue,
                        bgColor = StatusJustifiedBg,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Total Progress Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Porcentaje Global de Asistencia",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$rate%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (rate >= 80) StatusPresentGreen else StatusAbsentRed
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (rate / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (rate >= 80) StatusPresentGreen else StatusAbsentRed
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Calculado sobre ${report.totalSessions} clases registradas para este grado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Session History Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historial Sesión por Sesión (${report.historyItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (report.historyItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay sesiones de clase registradas aún para este grado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(report.historyItems, key = { it.session.id }) { item ->
                    SessionRecordHistoryRow(
                        item = item,
                        onEditClick = { itemToEdit = item }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SessionRecordHistoryRow(
    item: StudentSessionHistoryItem,
    onEditClick: () -> Unit
) {
    val session = item.session
    val record = item.record
    val status = record?.status ?: AttendanceRecordEntity.STATUS_AUSENTE

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
            .testTag("session_history_row_${session.id}"),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.topic,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${Formatters.formatDate(session.startTime)} • ${Formatters.formatTime(session.startTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = status, compact = true)
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_attendance_btn_${session.id}")
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

            // Reason / Motivo Badge if recorded
            if (!record?.note.isNullOrBlank()) {
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
                        text = "Motivo: ${record?.note}",
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

@Composable
private fun EditReportAttendanceDialog(
    item: StudentSessionHistoryItem,
    studentName: String,
    onDismiss: () -> Unit,
    onSave: (newStatus: String, reason: String) -> Unit
) {
    val session = item.session
    val initialStatus = item.record?.status ?: AttendanceRecordEntity.STATUS_AUSENTE
    var selectedStatus by remember { mutableStateOf(initialStatus) }
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
                            text = studentName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${session.topic} • ${Formatters.formatDate(session.startTime)}",
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
                                .testTag("edit_dialog_select_$statusKey"),
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
                        .testTag("attendance_edit_reason_input"),
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
                modifier = Modifier.testTag("save_report_attendance_btn")
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

