package com.example.ui.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.api.PronunciationAnalysis
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class PracticeState {
    IDLE,
    AI_SPEAKING,
    WAITING_FOR_USER,
    USER_RECORDING,
    ANALYZING,
    SHOWING_FEEDBACK,
    COMPLETED
}

class AppViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    // --- Core Database Flows ---

    val userSettings: StateFlow<UserSettingsEntity> = repository.userSettings
        .map { it ?: UserSettingsEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettingsEntity()
        )

    val allProgress: StateFlow<List<UserProgressEntity>> = repository.allProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allMistakes: StateFlow<List<UserMistakeEntity>> = repository.allMistakes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Active Practice Session State ---

    private val _activeDialogue = MutableStateFlow<Dialogue?>(null)
    val activeDialogue: StateFlow<Dialogue?> = _activeDialogue.asStateFlow()

    private val _currentTurnIndex = MutableStateFlow(0)
    val currentTurnIndex: StateFlow<Int> = _currentTurnIndex.asStateFlow()

    private val _practiceState = MutableStateFlow(PracticeState.IDLE)
    val practiceState: StateFlow<PracticeState> = _practiceState.asStateFlow()

    private val _currentSpokenText = MutableStateFlow("")
    val currentSpokenText: StateFlow<String> = _currentSpokenText.asStateFlow()

    private val _currentAnalysis = MutableStateFlow<PronunciationAnalysis?>(null)
    val currentAnalysis: StateFlow<PronunciationAnalysis?> = _currentAnalysis.asStateFlow()

    // Practicing history details of current session
    private var sessionStartTime: Long = 0L
    private val sessionScores = mutableListOf<Int>()
    private val sessionMistakes = mutableListOf<UserMistakeEntity>()

    // --- Initialization & Setup ---

    init {
        // Ensure default settings exist in the database
        viewModelScope.launch {
            repository.userSettings.firstOrNull() ?: run {
                repository.saveSettings(UserSettingsEntity())
            }
        }
    }

    // --- Settings Actions ---

    fun updateProfile(name: String, email: String, avatar: String) {
        viewModelScope.launch {
            val current = userSettings.value
            repository.saveSettings(current.copy(name = name, email = email, avatar = avatar))
        }
    }

    fun setThemeMode(isDark: Boolean) {
        viewModelScope.launch {
            val current = userSettings.value
            repository.saveSettings(current.copy(isDarkMode = isDark))
        }
    }

    fun setThemeColor(colorHex: String) {
        viewModelScope.launch {
            val current = userSettings.value
            repository.saveSettings(current.copy(themeColorHex = colorHex))
        }
    }

    fun setGeminiApiKey(apiKey: String) {
        viewModelScope.launch {
            val current = userSettings.value
            repository.saveSettings(current.copy(geminiApiKey = apiKey))
        }
    }

    fun loginWithGoogle(name: String, email: String, photoUrl: String?) {
        viewModelScope.launch {
            val current = userSettings.value
            repository.saveSettings(current.copy(
                isLoggedInWithGoogle = true,
                name = name,
                email = email,
                googlePhotoUrl = photoUrl ?: "",
                avatar = "google_user"
            ))
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            repository.clearAllProgress()
            val current = userSettings.value
            repository.saveSettings(UserSettingsEntity(
                id = 1,
                isDarkMode = current.isDarkMode,
                themeColorHex = current.themeColorHex,
                geminiApiKey = current.geminiApiKey,
                isLoggedInWithGoogle = false,
                googlePhotoUrl = ""
            ))
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.clearAllProgress()
        }
    }

    // --- Practice Session Operations ---

    fun startDialoguePractice(dialogue: Dialogue) {
        _activeDialogue.value = dialogue
        _currentTurnIndex.value = 0
        _practiceState.value = PracticeState.IDLE
        _currentSpokenText.value = ""
        _currentAnalysis.value = null
        sessionStartTime = SystemClock.elapsedRealtime()
        sessionScores.clear()
        sessionMistakes.clear()
        
        // Let's check the first turn. If it's AI, we should trigger AI speaking.
        val firstTurn = dialogue.turns.firstOrNull()
        if (firstTurn?.role == "AI") {
            _practiceState.value = PracticeState.AI_SPEAKING
        } else {
            _practiceState.value = PracticeState.WAITING_FOR_USER
        }
    }

    fun updateSpokenText(text: String) {
        _currentSpokenText.value = text
    }

    fun setPracticeState(state: PracticeState) {
        _practiceState.value = state
    }

    fun analyzeSpokenText(target: String, spoken: String) {
        if (_practiceState.value == PracticeState.ANALYZING) return
        _practiceState.value = PracticeState.ANALYZING
        
        viewModelScope.launch {
            val customKey = userSettings.value.geminiApiKey
            val result = GeminiClient.analyzePronunciation(target, spoken, customKey)
            _currentAnalysis.value = result
            _practiceState.value = PracticeState.SHOWING_FEEDBACK
            
            // Record metrics
            sessionScores.add(result.score)
            
            // Save mistakes to database
            result.mispronouncedWords.forEach { item ->
                val mistake = UserMistakeEntity(
                    word = item.word.lowercase().trim(),
                    count = 1, // Will be updated if it exists or we can load existing
                    lastSeenSentence = target,
                    correctionTip = item.tip,
                    lastPracticed = System.currentTimeMillis()
                )
                sessionMistakes.add(mistake)
                
                // Save to database
                launch {
                    val existing = allMistakes.value.find { it.word == mistake.word }
                    if (existing != null) {
                        repository.saveMistake(existing.copy(
                            count = existing.count + 1,
                            lastSeenSentence = target,
                            correctionTip = item.tip,
                            lastPracticed = System.currentTimeMillis()
                        ))
                    } else {
                        repository.saveMistake(mistake)
                    }
                }
            }
        }
    }

    fun advanceDialogue() {
        val dialogue = _activeDialogue.value ?: return
        val currentIndex = _currentTurnIndex.value
        
        if (currentIndex >= dialogue.turns.size - 1) {
            // End of dialogue!
            finishDialoguePractice()
        } else {
            // Advance to next turn
            val nextIndex = currentIndex + 1
            _currentTurnIndex.value = nextIndex
            _currentSpokenText.value = ""
            _currentAnalysis.value = null
            
            val nextTurn = dialogue.turns.getOrNull(nextIndex)
            if (nextTurn?.role == "AI") {
                _practiceState.value = PracticeState.AI_SPEAKING
            } else {
                _practiceState.value = PracticeState.WAITING_FOR_USER
            }
        }
    }

    private fun finishDialoguePractice() {
        _practiceState.value = PracticeState.COMPLETED
        val dialogue = _activeDialogue.value ?: return
        
        // Calculate and save overall progress metrics to Room
        val timeSpent = (SystemClock.elapsedRealtime() - sessionStartTime) / 1000
        val avgScore = if (sessionScores.isNotEmpty()) sessionScores.average().toFloat() else 100f
        
        viewModelScope.launch {
            val progress = UserProgressEntity(
                dialogueId = dialogue.id,
                isCompleted = true,
                turnsCompleted = dialogue.turns.size,
                totalTurns = dialogue.turns.size,
                averageScore = avgScore,
                timeSpentSeconds = timeSpent,
                lastPracticed = System.currentTimeMillis()
            )
            repository.saveProgress(progress)
        }
    }

    fun skipFeedbackAndAdvance() {
        advanceDialogue()
    }

    fun repeatCurrentTurn() {
        _currentSpokenText.value = ""
        _currentAnalysis.value = null
        val dialogue = _activeDialogue.value ?: return
        val currentTurn = dialogue.turns.getOrNull(_currentTurnIndex.value)
        if (currentTurn?.role == "AI") {
            _practiceState.value = PracticeState.AI_SPEAKING
        } else {
            _practiceState.value = PracticeState.WAITING_FOR_USER
        }
    }

    fun removeMistake(word: String) {
        viewModelScope.launch {
            repository.deleteMistake(word)
        }
    }
}
