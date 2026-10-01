package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teacher_profile")
data class TeacherProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Prof. Martín Ramírez",
    val school: String = "Instituto Educativo San Miguel",
    val subject: String = "Ciencias y Matemáticas",
    val email: String = "martin.ramirez@escuela.edu",
    val defaultSessionDurationMinutes: Int = 15,
    val lateToleranceMinutes: Int = 5,
    val autoMarkAbsentOnFinish: Boolean = true
)
