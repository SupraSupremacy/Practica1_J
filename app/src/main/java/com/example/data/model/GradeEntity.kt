package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grades")
data class GradeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val section: String,
    val subject: String,
    val colorHex: String = "#1A56DB",
    val schoolYear: String = "2026",
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = if (section.isNotBlank()) "$name - $section" else name
}
