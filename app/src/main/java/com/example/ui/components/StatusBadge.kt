package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecordEntity
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusJustifiedBg
import com.example.ui.theme.StatusJustifiedBlue
import com.example.ui.theme.StatusLateAmber
import com.example.ui.theme.StatusLateBg
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusPresentGreen

@Composable
fun StatusBadge(
    status: String?,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (bgColor, textColor, text, icon) = when (status) {
        AttendanceRecordEntity.STATUS_PRESENTE -> Quadruple(
            StatusPresentBg,
            StatusPresentGreen,
            "Presente",
            Icons.Filled.CheckCircle
        )
        AttendanceRecordEntity.STATUS_RETARDO -> Quadruple(
            StatusLateBg,
            StatusLateAmber,
            "Retardo",
            Icons.Filled.Schedule
        )
        AttendanceRecordEntity.STATUS_JUSTIFICADO -> Quadruple(
            StatusJustifiedBg,
            StatusJustifiedBlue,
            "Justificado",
            Icons.Filled.Help
        )
        AttendanceRecordEntity.STATUS_AUSENTE -> Quadruple(
            StatusAbsentBg,
            StatusAbsentRed,
            "Ausente",
            Icons.Filled.Cancel
        )
        else -> Quadruple(
            Color(0xFFF3F4F6),
            Color(0xFF6B7280),
            "Sin registro",
            Icons.Filled.Error
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 4.dp else 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = textColor,
                modifier = Modifier.size(if (compact) 14.dp else 16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
