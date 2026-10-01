package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AttendanceViewModel
import com.example.ui.components.AppBottomNavigation
import com.example.ui.screens.GenerateQrScreen
import com.example.ui.screens.GradeDetailScreen
import com.example.ui.screens.GradesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SessionDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAttendanceApp()
            }
        }
    }
}

@Composable
fun MainAttendanceApp(
    viewModel: AttendanceViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedGradeId by viewModel.selectedGradeId.collectAsState()
    val selectedGrade by viewModel.selectedGrade.collectAsState()
    val selectedSessionId by viewModel.selectedSessionId.collectAsState()
    val selectedSession by viewModel.selectedSession.collectAsState()
    val selectedReportStudentId by viewModel.selectedReportStudentId.collectAsState()

    // Handle back button for sub-screens
    BackHandler(enabled = selectedReportStudentId != null) {
        viewModel.selectReportStudent(null)
    }

    BackHandler(enabled = selectedReportStudentId == null && selectedSessionId != null) {
        viewModel.selectSession(null)
    }

    BackHandler(enabled = selectedReportStudentId == null && selectedSessionId == null && selectedGradeId != null) {
        viewModel.selectGrade(null)
    }

    BackHandler(enabled = selectedReportStudentId == null && selectedSessionId == null && selectedGradeId == null && currentTab != 0) {
        viewModel.switchTab(0)
    }

    val showBottomBar = selectedGradeId == null && selectedSessionId == null && selectedReportStudentId == null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(
                    selectedTab = currentTab,
                    onTabSelected = { tab -> viewModel.switchTab(tab) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> {
                    GenerateQrScreen(viewModel = viewModel)
                }
                1 -> {
                    when {
                        selectedSessionId != null && selectedSession != null -> {
                            SessionDetailScreen(
                                session = selectedSession!!,
                                grade = selectedGrade,
                                viewModel = viewModel,
                                onBack = { viewModel.selectSession(null) }
                            )
                        }
                        selectedGradeId != null && selectedGrade != null -> {
                            GradeDetailScreen(
                                grade = selectedGrade!!,
                                viewModel = viewModel,
                                onBack = { viewModel.selectGrade(null) },
                                onSelectSession = { sessionId -> viewModel.selectSession(sessionId) }
                            )
                        }
                        else -> {
                            GradesScreen(viewModel = viewModel)
                        }
                    }
                }
                2 -> {
                    ReportsScreen(viewModel = viewModel)
                }
                3 -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
