package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TeacherProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherProfileDao {
    @Query("SELECT * FROM teacher_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<TeacherProfileEntity?>

    @Query("SELECT * FROM teacher_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): TeacherProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: TeacherProfileEntity)

    @Update
    suspend fun updateProfile(profile: TeacherProfileEntity)
}
