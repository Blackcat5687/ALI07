package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "مستخدم متدرب",
    val email: String = "learner@englishtalk.com",
    val avatar: String = "avatar_1", // identifier for avatars (e.g. avatar_1 to avatar_5)
    val isDarkMode: Boolean = false,
    val themeColorHex: String = "#0D9488", // Teal as default modern aesthetic accent
    val geminiApiKey: String = "", // Gemini API Key
    val isLoggedInWithGoogle: Boolean = false,
    val googlePhotoUrl: String = ""
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val dialogueId: String,
    val isCompleted: Boolean = false,
    val turnsCompleted: Int = 0,
    val totalTurns: Int = 0,
    val averageScore: Float = 0f,
    val timeSpentSeconds: Long = 0L,
    val lastPracticed: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_mistakes")
data class UserMistakeEntity(
    @PrimaryKey val word: String, // The mispronounced word in lowercase (e.g. "comfortable")
    val count: Int = 1,
    val lastSeenSentence: String = "",
    val correctionTip: String = "", // AI advice on how to pronounce it correctly
    val lastPracticed: Long = System.currentTimeMillis()
)
