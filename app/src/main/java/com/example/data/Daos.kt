package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: UserSettingsEntity)
}

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress")
    fun getAllProgress(): Flow<List<UserProgressEntity>>

    @Query("SELECT * FROM user_progress WHERE dialogueId = :dialogueId LIMIT 1")
    fun getProgressForDialogue(dialogueId: String): Flow<UserProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: UserProgressEntity)

    @Query("DELETE FROM user_progress")
    suspend fun clearAll()
}

@Dao
interface UserMistakeDao {
    @Query("SELECT * FROM user_mistakes ORDER BY count DESC")
    fun getAllMistakes(): Flow<List<UserMistakeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(mistake: UserMistakeEntity)

    @Query("DELETE FROM user_mistakes WHERE word = :word")
    suspend fun delete(word: String)

    @Query("DELETE FROM user_mistakes")
    suspend fun clearAll()
}
