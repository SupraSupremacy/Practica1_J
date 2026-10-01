package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AttendanceRepository
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.GradeEntity
import com.example.data.model.StudentAttendanceSummary
import com.example.data.model.StudentEntity
import com.example.data.model.StudentWithAttendance
import com.example.data.model.TeacherProfileEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = AttendanceRepository(
        database.gradeDao(),
        database.studentDao(),
        database.attendanceDao(),
        database.teacherProfileDao()
    )

    // Navigation & Tab state
    // 0 = Generar QR, 1 = Grados, 2 = Ajustes
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _selectedGradeId = MutableStateFlow<Long?>(null)
    val selectedGradeId: StateFlow<Long?> = _selectedGradeId.asStateFlow()

    private val _selectedSessionId = MutableStateFlow<Long?>(null)
    val selectedSessionId: StateFlow<Long?> = _selectedSessionId.asStateFlow()

    private val _selectedStudentForBadge = MutableStateFlow<StudentEntity?>(null)
    val selectedStudentForBadge: StateFlow<StudentEntity?> = _selectedStudentForBadge.asStateFlow()

    private val _isQrFullScreen = MutableStateFlow(false)
    val isQrFullScreen: StateFlow<Boolean> = _isQrFullScreen.asStateFlow()

    private val _lastScannedStudent = MutableStateFlow<StudentEntity?>(null)
    val lastScannedStudent: StateFlow<StudentEntity?> = _lastScannedStudent.asStateFlow()

    private val _activeSessionTimerSeconds = MutableStateFlow(0L)
    val activeSessionTimerSeconds: StateFlow<Long> = _activeSessionTimerSeconds.asStateFlow()

    private var timerJob: Job? = null

    // Base Data Flows
    val allGrades: StateFlow<List<GradeEntity>> = repository.allGrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSession: StateFlow<AttendanceSessionEntity?> = repository.activeSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val teacherProfile: StateFlow<TeacherProfileEntity?> = repository.teacherProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSessions: StateFlow<List<AttendanceSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStudentsCount: StateFlow<Int> = repository.totalStudentsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Active session details
    val activeSessionGrade: StateFlow<GradeEntity?> = activeSession.flatMapLatest { session ->
        if (session != null) {
            repository.getGrade(session.gradeId)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeSessionStudents: StateFlow<List<StudentWithAttendance>> = activeSession.flatMapLatest { session ->
        if (session != null) {
            repository.getStudentsWithAttendanceForSession(session.gradeId, session.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Grade details
    val selectedGrade: StateFlow<GradeEntity?> = _selectedGradeId.flatMapLatest { id ->
        if (id != null) repository.getGrade(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedGradeStudents: StateFlow<List<StudentEntity>> = _selectedGradeId.flatMapLatest { id ->
        if (id != null) repository.getStudentsByGrade(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedGradeSummaries: StateFlow<List<StudentAttendanceSummary>> = _selectedGradeId.flatMapLatest { id ->
        if (id != null) repository.getStudentSummariesForGrade(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedGradeSessions: StateFlow<List<AttendanceSessionEntity>> = _selectedGradeId.flatMapLatest { id ->
        if (id != null) repository.getSessionsForGrade(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Session inspection
    val selectedSession: StateFlow<AttendanceSessionEntity?> = _selectedSessionId.flatMapLatest { id ->
        if (id != null) repository.getSession(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedSessionStudents: StateFlow<List<StudentWithAttendance>> = combine(
        _selectedSessionId,
        selectedSession
    ) { _, session ->
        session
    }.flatMapLatest { session ->
        if (session != null) {
            repository.getStudentsWithAttendanceForSession(session.gradeId, session.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureDefaultDataLoaded()
        }

        // Manage active timer
        viewModelScope.launch {
            activeSession.collect { session ->
                if (session != null) {
                    startTimer(session.startTime)
                } else {
                    stopTimer()
                }
            }
        }
    }

    private fun startTimer(startTimeMillis: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000
                _activeSessionTimerSeconds.value = elapsed.coerceAtLeast(0)
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _activeSessionTimerSeconds.value = 0
    }

    fun switchTab(tabIndex: Int) {
        _currentTab.value = tabIndex
        if (tabIndex != 1) {
            _selectedGradeId.value = null
            _selectedSessionId.value = null
        }
    }

    fun selectGrade(gradeId: Long?) {
        _selectedGradeId.value = gradeId
        _selectedSessionId.value = null
    }

    fun selectSession(sessionId: Long?) {
        _selectedSessionId.value = sessionId
    }

    fun openStudentBadge(student: StudentEntity?) {
        _selectedStudentForBadge.value = student
    }

    fun setQrFullScreen(fullScreen: Boolean) {
        _isQrFullScreen.value = fullScreen
    }

    fun startQrSession(gradeId: Long, topic: String, durationMinutes: Int) {
        viewModelScope.launch {
            val session = repository.startNewSession(gradeId, topic, durationMinutes)
            _currentTab.value = 0 // Switch to QR tab
        }
    }

    fun recordAttendance(studentId: Long, status: String) {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            repository.recordStudentAttendance(session.id, studentId, session.gradeId, status)
        }
    }

    fun markRemainingAbsent() {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            val students = activeSessionStudents.value
            for (item in students) {
                if (item.record == null) {
                    repository.recordStudentAttendance(
                        session.id,
                        item.student.id,
                        session.gradeId,
                        AttendanceRecordEntity.STATUS_AUSENTE
                    )
                }
            }
        }
    }

    fun simulateStudentScan(student: StudentEntity) {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            repository.recordStudentAttendance(
                session.id,
                student.id,
                session.gradeId,
                AttendanceRecordEntity.STATUS_PRESENTE
            )
            _lastScannedStudent.value = student
            delay(3000)
            if (_lastScannedStudent.value?.id == student.id) {
                _lastScannedStudent.value = null
            }
        }
    }

    fun checkInByCode(code: String) {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            val student = repository.recordAttendanceByCode(session.id, session.gradeId, code)
            if (student != null) {
                _lastScannedStudent.value = student
                delay(3000)
                if (_lastScannedStudent.value?.id == student.id) {
                    _lastScannedStudent.value = null
                }
            }
        }
    }

    fun finishActiveSession() {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            val profile = teacherProfile.value
            val autoMarkAbsent = profile?.autoMarkAbsentOnFinish ?: true
            repository.finishSession(session.id, session.gradeId, autoMarkAbsent)
            stopTimer()
        }
    }

    fun updateSessionRecord(
        sessionId: Long,
        studentId: Long,
        gradeId: Long,
        status: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.recordStudentAttendance(sessionId, studentId, gradeId, status, note)
        }
    }

    fun createGrade(name: String, section: String, subject: String, colorHex: String) {
        viewModelScope.launch {
            repository.createGrade(name, section, subject, colorHex)
        }
    }

    fun deleteGrade(gradeId: Long) {
        viewModelScope.launch {
            if (_selectedGradeId.value == gradeId) {
                _selectedGradeId.value = null
            }
            repository.deleteGrade(gradeId)
        }
    }

    fun addStudent(gradeId: Long, fullName: String, studentCode: String, email: String, phone: String) {
        viewModelScope.launch {
            repository.addStudent(gradeId, fullName, studentCode, email, phone)
        }
    }

    fun deleteStudent(studentId: Long) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
        }
    }

    fun updateTeacherProfile(profile: TeacherProfileEntity) {
        viewModelScope.launch {
            repository.updateTeacherProfile(profile)
        }
    }

    fun reloadDemoData() {
        viewModelScope.launch {
            repository.loadDemoData()
            _selectedGradeId.value = null
            _selectedSessionId.value = null
        }
    }

    fun exportAllToCsv(context: android.content.Context) {
        viewModelScope.launch {
            val grades = allGrades.value
            val allStudents = mutableListOf<StudentEntity>()
            for (g in grades) {
                allStudents.addAll(repository.getStudentsByGradeSync(g.id))
            }
            val records = repository.getAllRecordsSync()
            val sessions = allSessions.value
            com.example.util.CsvExporter.exportOverallReport(context, grades, allStudents, records, sessions)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _selectedGradeId.value = null
            _selectedSessionId.value = null
        }
    }
}
