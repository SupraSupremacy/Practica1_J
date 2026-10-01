package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = GradeEntity::class,
            parentColumns = ["id"],
            childColumns = ["gradeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["gradeId"])]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gradeId: Long,
    val fullName: String,
    val studentCode: String,
    val email: String = "",
    val phone: String = "",
    val avatarColorHex: String = "#1E429F",
    val createdAt: Long = System.currentTimeMillis()
)
