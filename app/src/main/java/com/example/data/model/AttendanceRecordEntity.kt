package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = AttendanceSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId", "studentId"], unique = true),
        Index(value = ["studentId"]),
        Index(value = ["gradeId"])
    ]
)
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val studentId: Long,
    val gradeId: Long,
    val status: String = STATUS_PRESENTE, // PRESENTE, RETARDO, JUSTIFICADO, AUSENTE
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
) {
    companion object {
        const val STATUS_PRESENTE = "PRESENTE"
        const val STATUS_RETARDO = "RETARDO"
        const val STATUS_JUSTIFICADO = "JUSTIFICADO"
        const val STATUS_AUSENTE = "AUSENTE"
    }
}
