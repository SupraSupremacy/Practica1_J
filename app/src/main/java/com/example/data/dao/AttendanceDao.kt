package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    // --- Sessions ---
    @Query("SELECT * FROM attendance_sessions WHERE isActive = 1 ORDER BY startTime DESC LIMIT 1")
    fun getActiveSession(): Flow<AttendanceSessionEntity?>

    @Query("SELECT * FROM attendance_sessions WHERE isActive = 1 ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSessionSync(): AttendanceSessionEntity?

    @Query("SELECT * FROM attendance_sessions WHERE id = :id LIMIT 1")
    fun getSessionById(id: Long): Flow<AttendanceSessionEntity?>

    @Query("SELECT * FROM attendance_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionByIdSync(id: Long): AttendanceSessionEntity?

    @Query("SELECT * FROM attendance_sessions WHERE gradeId = :gradeId ORDER BY startTime DESC")
    fun getSessionsByGrade(gradeId: Long): Flow<List<AttendanceSessionEntity>>

    @Query("SELECT * FROM attendance_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<AttendanceSessionEntity>>

    @Query("SELECT COUNT(*) FROM attendance_sessions")
    fun getTotalSessionsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AttendanceSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<AttendanceSessionEntity>): List<Long>

    @Update
    suspend fun updateSession(session: AttendanceSessionEntity)

    @Query("UPDATE attendance_sessions SET isActive = 0, endTime = :endTime WHERE id = :id")
    suspend fun closeSession(id: Long, endTime: Long = System.currentTimeMillis())

    @Query("UPDATE attendance_sessions SET isActive = 0 WHERE isActive = 1")
    suspend fun closeAllActiveSessions()

    @Delete
    suspend fun deleteSession(session: AttendanceSessionEntity)

    @Query("DELETE FROM attendance_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    // --- Records ---
    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getRecordsBySession(sessionId: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getRecordsBySessionSync(sessionId: Long): List<AttendanceRecordEntity>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getRecordsByStudent(studentId: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE gradeId = :gradeId")
    fun getRecordsByGrade(gradeId: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId AND studentId = :studentId LIMIT 1")
    suspend fun getRecordForStudentInSession(sessionId: Long, studentId: Long): AttendanceRecordEntity?

    @Query("SELECT * FROM attendance_records")
    fun getAllRecords(): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records")
    suspend fun getAllRecordsSync(): List<AttendanceRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecordEntity>): List<Long>

    @Update
    suspend fun updateRecord(record: AttendanceRecordEntity)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun deleteRecordsBySession(sessionId: Long)

    @Query("DELETE FROM attendance_sessions")
    suspend fun clearAllSessions()

    @Query("DELETE FROM attendance_records")
    suspend fun clearAllRecords()
}
