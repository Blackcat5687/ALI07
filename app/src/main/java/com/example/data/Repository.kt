package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val userSettingsDao: UserSettingsDao,
    private val userProgressDao: UserProgressDao,
    private val userMistakeDao: UserMistakeDao
) {
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getSettings()
    val allProgress: Flow<List<UserProgressEntity>> = userProgressDao.getAllProgress()
    val allMistakes: Flow<List<UserMistakeEntity>> = userMistakeDao.getAllMistakes()

    fun getProgressForDialogue(dialogueId: String): Flow<UserProgressEntity?> {
        return userProgressDao.getProgressForDialogue(dialogueId)
    }

    suspend fun saveSettings(settings: UserSettingsEntity) {
        userSettingsDao.insertOrUpdate(settings)
    }

    suspend fun saveProgress(progress: UserProgressEntity) {
        userProgressDao.insertOrUpdate(progress)
    }

    suspend fun saveMistake(mistake: UserMistakeEntity) {
        userMistakeDao.insertOrUpdate(mistake)
    }

    suspend fun deleteMistake(word: String) {
        userMistakeDao.delete(word)
    }

    suspend fun clearAllProgress() {
        userProgressDao.clearAll()
        userMistakeDao.clearAll()
    }
}
