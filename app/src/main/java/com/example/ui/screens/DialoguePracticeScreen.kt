package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Dialogue
import com.example.data.DialogueTurn
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.PracticeState
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialoguePracticeScreen(
    viewModel: AppViewModel,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dialogue by viewModel.activeDialogue.collectAsState()
    val currentTurnIndex by viewModel.currentTurnIndex.collectAsState()
    val practiceState by viewModel.practiceState.collectAsState()
    val spokenText by viewModel.currentSpokenText.collectAsState()
    val currentAnalysis by viewModel.currentAnalysis.collectAsState()

    val listState = rememberLazyListState()

    // --- Speech & TTS Engine Config ---
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var isRecognizerListening by remember { mutableStateOf(false) }
    var micPermissionGranted by remember { mutableStateOf(true) }
    var manualTextInput by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    // --- Gemini Multimodal Live API Call Mode ---
    var practiceMode by remember { mutableStateOf("LIVE_CALL") } // "LIVE_CALL" or "INDIVIDUAL_EVAL"
    val settings by viewModel.userSettings.collectAsState()
    val apiKey = if (settings.geminiApiKey.isNotBlank()) {
        settings.geminiApiKey
    } else {
        com.example.BuildConfig.GEMINI_API_KEY
    }

    var liveClientState by remember { mutableStateOf(com.example.api.GeminiLiveClient.LiveState.IDLE) }
    var liveErrorMsg by remember { mutableStateOf<String?>(null) }
    var isLiveActive by remember { mutableStateOf(false) }

    val sysInstruction = remember(dialogue) {
        val dialogueContext = dialogue?.let { d ->
            "Dialogue Context:\nTitle: ${d.title}\nCategory: ${d.category}\nLevel: ${d.level}\nTurns:\n" +
            d.turns.joinToString("\n") { "${it.role}: ${it.text}" }
        } ?: "No active dialogue context"
        
        """
        You are a highly supportive, warm, and friendly native English speaking tutor talking with an Arabic-speaking learner.
        Your task is to conduct a real-time, continuous, bi-directional voice call conversation. 
        Actively use the following dialogue script as the context/guide for this call:
        $dialogueContext
        
        Guidance Rules:
        1. Guide the user through the dialogue. Help them practice their conversational english and pronunciation.
        2. Correct any pronunciation, spelling, or grammatical errors they make gently, warmly, and encouragingly.
        3. Speak primarily in English, but you can provide brief, helpful explanations or corrections in Arabic when needed.
        4. Keep your responses short, natural, and highly conversational to mimic a real telephone call. Do not give extremely long monologues.
        5. Be very friendly, patient, and motivating.
        """.trimIndent()
    }

    val liveClient = remember(apiKey, sysInstruction) {
        com.example.api.GeminiLiveClient(
            apiKey = apiKey,
            systemInstructionText = sysInstruction,
            listener = object : com.example.api.GeminiLiveClient.LiveClientListener {
                override fun onStateChanged(state: com.example.api.GeminiLiveClient.LiveState) {
                    liveClientState = state
                    isLiveActive = (state == com.example.api.GeminiLiveClient.LiveState.LISTENING || state == com.example.api.GeminiLiveClient.LiveState.SPEAKING)
                }

                override fun onError(message: String) {
                    liveErrorMsg = message
                }

                override fun onInterrupted() {
                    Log.d("DialoguePracticeScreen", "AI was interrupted by user voice input")
                }
            }
        )
    }

    DisposableEffect(liveClient) {
        onDispose {
            liveClient.stop()
        }
    }

    val liveMicPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        micPermissionGranted = isGranted
        if (isGranted) {
            liveClient.start()
        } else {
            Toast.makeText(context, "صلاحية المايكروفون مطلوبة للمحادثة الصوتية المستمرة.", Toast.LENGTH_LONG).show()
        }
    }

    val toggleLiveCall: () -> Unit = {
        if (isLiveActive || liveClientState == com.example.api.GeminiLiveClient.LiveState.CONNECTING) {
            liveClient.stop()
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            
            if (hasPermission) {
                micPermissionGranted = true
                liveClient.start()
            } else {
                liveMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    DisposableEffect(context) {
        val ttsObj = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
            }
        }
        ttsObj.language = Locale.US
        tts = ttsObj
        onDispose {
            ttsObj.stop()
            ttsObj.shutdown()
        }
    }

    DisposableEffect(context) {
        var recognizerObj: SpeechRecognizer? = null
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                recognizerObj = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer = recognizerObj
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        onDispose {
            recognizerObj?.destroy()
        }
    }

    val speakAI: (String) -> Unit = { text ->
        if (ttsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AI_SPEECH_UTTERANCE")
        }
    }

    val currentTurn = dialogue?.turns?.getOrNull(currentTurnIndex)
    LaunchedEffect(currentTurnIndex, practiceState) {
        if (currentTurn != null && currentTurn.role == "AI" && practiceState == PracticeState.AI_SPEAKING) {
            speakAI(currentTurn.text)
            kotlinx.coroutines.delay(2500)
            viewModel.setPracticeState(PracticeState.WAITING_FOR_USER)
        }
        if (currentTurnIndex >= 0 && dialogue != null) {
            listState.animateScrollToItem(currentTurnIndex)
        }
    }

    val startListening: () -> Unit = {
        if (speechRecognizer == null) {
            showManualInput = true
            Toast.makeText(context, "خدمة التعرف على الصوت غير متاحة على هذا الجهاز. يمكنك كتابة الجملة يدوياً الآن.", Toast.LENGTH_LONG).show()
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isRecognizerListening = true
                    viewModel.setPracticeState(PracticeState.USER_RECORDING)
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isRecognizerListening = false
                }
                override fun onError(error: Int) {
                    isRecognizerListening = false
                    viewModel.setPracticeState(PracticeState.WAITING_FOR_USER)
                    if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                        micPermissionGranted = false
                    }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    viewModel.updateSpokenText(text)
                    
                    if (text.isNotBlank() && currentTurn != null) {
                        viewModel.analyzeSpokenText(currentTurn.text, text)
                    } else {
                        viewModel.setPracticeState(PracticeState.WAITING_FOR_USER)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.firstOrNull()?.let { viewModel.updateSpokenText(it) }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            try {
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                showManualInput = true
                Toast.makeText(context, "حدث خطأ أثناء بدء التعرف على الصوت: ${e.localizedMessage}. يمكنك كتابة الجملة يدوياً.", Toast.LENGTH_LONG).show()
            }
        }
    }

    val stopListening: () -> Unit = {
        try {
            speechRecognizer?.stopListening()
            isRecognizerListening = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        micPermissionGranted = isGranted
        if (isGranted) {
            startListening()
        } else {
            Toast.makeText(context, "صلاحية المايكروفون مطلوبة لتسجيل نطقك وتدريبه.", Toast.LENGTH_LONG).show()
        }
    }

    val checkAndStartListening: () -> Unit = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            micPermissionGranted = true
            startListening()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            dialogue?.title ?: "صفحة التدريب",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "عودة"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showManualInput = !showManualInput }) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "إدخال نص يدوي"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (dialogue == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("جاري تحميل الحوار...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                // 1. Segmented Step Progress Indicator (Phone 5 & 6)
                val totalSteps = dialogue!!.turns.size
                StepProgressBar(
                    totalSteps = totalSteps,
                    currentStep = currentTurnIndex,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )

                // 2. Chat Feed Scroll
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp)
                ) {
                    itemsIndexed(dialogue!!.turns) { index, turn ->
                        val isTurnActive = index == currentTurnIndex
                        val isTurnCompleted = index < currentTurnIndex

                        DialogueBubble(
                            turn = turn,
                            isActive = isTurnActive,
                            isCompleted = isTurnCompleted,
                            onPlayAudio = { speakAI(turn.text) }
                        )
                    }
                }

                // 3. Floating Console Control Panel (Matches design of Phone 5 and 6)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Switcher between Mode: Live Phone Call & Individual Evaluation
                        TabRow(
                            selectedTabIndex = if (practiceMode == "LIVE_CALL") 0 else 1,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(19.dp)),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            indicator = { Box {} }, // Hide standard indicator
                            divider = {}
                        ) {
                            Tab(
                                selected = practiceMode == "LIVE_CALL",
                                onClick = { 
                                    practiceMode = "LIVE_CALL"
                                    stopListening()
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(if (practiceMode == "LIVE_CALL") MaterialTheme.colorScheme.primary else Color.Transparent)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (practiceMode == "LIVE_CALL") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "مكالمة حية مستمرة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (practiceMode == "LIVE_CALL") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Tab(
                                selected = practiceMode == "INDIVIDUAL_EVAL",
                                onClick = { 
                                    practiceMode = "INDIVIDUAL_EVAL"
                                    liveClient.stop()
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(if (practiceMode == "INDIVIDUAL_EVAL") MaterialTheme.colorScheme.primary else Color.Transparent)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (practiceMode == "INDIVIDUAL_EVAL") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "تقييم نطق فردي",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (practiceMode == "INDIVIDUAL_EVAL") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (practiceMode == "LIVE_CALL") {
                            // --- Live Call Mode UI Console ---
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                liveErrorMsg?.let { errorMsg ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                    ) {
                                        Text(
                                            text = errorMsg,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(12.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                when (liveClientState) {
                                    com.example.api.GeminiLiveClient.LiveState.IDLE, 
                                    com.example.api.GeminiLiveClient.LiveState.ERROR -> {
                                        Text(
                                            "اضغط لبدء محادثة صوتية مستمرة وتلقائية مع معلم الذكاء الاصطناعي. تحدث واسمع ردوده فوراً دون أزرار!",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(18.dp))
                                        
                                        // Big Start Call Button
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            FloatingActionButton(
                                                onClick = { toggleLiveCall() },
                                                containerColor = Color(0xFF10B981),
                                                contentColor = Color.White,
                                                shape = CircleShape,
                                                modifier = Modifier.size(62.dp)
                                            ) {
                                                Icon(Icons.Default.Phone, contentDescription = "بدء المكالمة", modifier = Modifier.size(30.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("بدء المحادثة الصوتية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF10B981))
                                    }

                                    com.example.api.GeminiLiveClient.LiveState.CONNECTING -> {
                                        CircularProgressIndicator(modifier = Modifier.size(36.dp), color = Color(0xFF3B82F6))
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            "جاري الاتصال بمعلم الذكاء الاصطناعي الخاص بك...",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF3B82F6)
                                        )
                                        Spacer(modifier = Modifier.height(18.dp))
                                        
                                        // End Call Button
                                        Button(
                                            onClick = { toggleLiveCall() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.CallEnd, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("إلغاء الاتصال", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    com.example.api.GeminiLiveClient.LiveState.LISTENING -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "المعلم يستمع إليك الآن... تحدث بالإنجليزية!",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "يمكنك التحدث بالإنجليزية بحرية الآن، وسيقوم المعلم بالرد عليك وتصحيح نطقك فوراً.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Pulse Green Call Button to End
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(Color.Red.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            FloatingActionButton(
                                                onClick = { toggleLiveCall() },
                                                containerColor = Color.Red,
                                                contentColor = Color.White,
                                                shape = CircleShape,
                                                modifier = Modifier.size(62.dp)
                                            ) {
                                                Icon(Icons.Default.CallEnd, contentDescription = "إنهاء المكالمة", modifier = Modifier.size(30.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("إنهاء المحادثة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Red)
                                    }

                                    com.example.api.GeminiLiveClient.LiveState.SPEAKING -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF3B82F6))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "المعلم يتحدث الآن... استمع بتركيز",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF3B82F6)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "ملاحظة: يمكنك مقاطعة المعلم في أي وقت والتحدث مباشرة!",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Call End Button
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(Color.Red.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            FloatingActionButton(
                                                onClick = { toggleLiveCall() },
                                                containerColor = Color.Red,
                                                contentColor = Color.White,
                                                shape = CircleShape,
                                                modifier = Modifier.size(62.dp)
                                            ) {
                                                Icon(Icons.Default.CallEnd, contentDescription = "إنهاء المكالمة", modifier = Modifier.size(30.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("إنهاء المحادثة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Red)
                                    }
                                }
                            }
                        } else {
                            // --- Original Individual Evaluation Mode UI Console ---
                            AnimatedContent(
                                targetState = practiceState,
                                label = "حالة التدريب"
                            ) { state ->
                                when (state) {
                                    PracticeState.IDLE -> {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        ) {
                                            Text(
                                                "اضغط على زر البدء لتبدأ المحادثة الصوتية!",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF3B82F6)
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Button(
                                                onClick = { viewModel.setPracticeState(PracticeState.AI_SPEAKING) },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                                            ) {
                                                Text("ابدأ المحادثة الآن", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    PracticeState.AI_SPEAKING -> {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "المعلم الذكي يتكلم الآن... استمع بتركيز",
                                                fontSize = 14.sp,
                                                color = Color(0xFF3B82F6),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            LinearProgressIndicator(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.6f)
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                color = Color(0xFF3B82F6)
                                            )
                                        }
                                    }

                                    PracticeState.WAITING_FOR_USER -> {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "دورك الآن! انطق الجملة التالية:",
                                                fontSize = 13.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF3B82F6).copy(alpha = 0.08f)),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.2f))
                                            ) {
                                                Text(
                                                    text = currentTurn?.text ?: "",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E3A8A),
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(18.dp))

                                            // Console Buttons: Restart | Mic | Pause
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CircularControlButton(
                                                    icon = Icons.Default.Refresh,
                                                    label = "إعادة",
                                                    onClick = { viewModel.repeatCurrentTurn() }
                                                )

                                                // Main Pulse Microphone
                                                Box(
                                                    modifier = Modifier
                                                        .size(76.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    FloatingActionButton(
                                                        onClick = { checkAndStartListening() },
                                                        containerColor = Color(0xFF3B82F6),
                                                        contentColor = Color.White,
                                                        shape = CircleShape,
                                                        modifier = Modifier.size(58.dp)
                                                    ) {
                                                        Icon(Icons.Default.Mic, contentDescription = "تحدث الآن", modifier = Modifier.size(28.dp))
                                                    }
                                                }

                                                CircularControlButton(
                                                    icon = Icons.Default.Pause,
                                                    label = "توقف مؤقت",
                                                    onClick = { }
                                                )
                                            }
                                        }
                                    }

                                    PracticeState.USER_RECORDING -> {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "الميكروفون نشط... تحدث الآن",
                                                color = Color.Red,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CircularControlButton(
                                                    icon = Icons.Default.Refresh,
                                                    label = "إعادة",
                                                    onClick = { viewModel.repeatCurrentTurn() }
                                                )

                                                // Recording Red Mic Pulsing Box
                                                Box(
                                                    modifier = Modifier
                                                        .size(76.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Red.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    FloatingActionButton(
                                                        onClick = { stopListening() },
                                                        containerColor = Color.Red,
                                                        contentColor = Color.White,
                                                        shape = CircleShape,
                                                        modifier = Modifier.size(58.dp)
                                                    ) {
                                                        Icon(Icons.Default.MicNone, contentDescription = "إيقاف", modifier = Modifier.size(28.dp))
                                                    }
                                                }

                                                CircularControlButton(
                                                    icon = Icons.Default.Pause,
                                                    label = "توقف مؤقت",
                                                    onClick = { }
                                                )
                                            }

                                            if (spokenText.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                    "الترجمة المستلمة: \"$spokenText\"",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }

                                    PracticeState.ANALYZING -> {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator(modifier = Modifier.size(36.dp), color = Color(0xFF3B82F6))
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(
                                                "يقوم المعلم بتوصيل صوتك إلى خوادم Gemini AI...",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF3B82F6)
                                            )
                                        }
                                    }

                                    PracticeState.SHOWING_FEEDBACK -> {
                                        val analysis = currentAnalysis
                                        if (analysis != null) {
                                            val scoreColor = when {
                                                analysis.score >= 85 -> Color(0xFF10B981)
                                                analysis.score >= 70 -> Color(0xFFF59E0B)
                                                else -> Color(0xFFEF4444)
                                            }

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                // Circular Score Display
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        "جودة نطق العبارة: ",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                    Text(
                                                        "${analysis.score}%",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 22.sp,
                                                        color = scoreColor
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Text(
                                                    analysis.feedback,
                                                    textAlign = TextAlign.Center,
                                                    fontSize = 13.sp,
                                                    lineHeight = 20.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                if (analysis.mispronouncedWords.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Text(
                                                        "كلمات بحاجة لتدريب:",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color.Red
                                                    )
                                                    LazyRow(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        modifier = Modifier.padding(top = 4.dp),
                                                        reverseLayout = true
                                                    ) {
                                                        items(analysis.mispronouncedWords) { wordItem ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(Color.Red.copy(alpha = 0.1f))
                                                                    .border(1.dp, Color.Red.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                                            ) {
                                                                Text(
                                                                    wordItem.word,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.Red
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(18.dp))

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    OutlinedButton(
                                                        onClick = { viewModel.repeatCurrentTurn() },
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("إعادة اللفظ", fontWeight = FontWeight.Bold)
                                                    }

                                                    Button(
                                                        onClick = { viewModel.advanceDialogue() },
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                                        modifier = Modifier.weight(1.5f)
                                                    ) {
                                                        Text("الجملة التالية", fontWeight = FontWeight.Bold)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    PracticeState.COMPLETED -> {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "تم الاكتمال",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(56.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "رائع! لقد أنهيت الحوار بالكامل!",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color(0xFF10B981)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = onBackClicked,
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                modifier = Modifier.fillMaxWidth(0.7f)
                                            ) {
                                                Text("العودة للقائمة", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // --- Optional Manual Input ---
                        AnimatedVisibility(visible = showManualInput) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            ) {
                                Divider(modifier = Modifier.padding(bottom = 12.dp))
                                Text(
                                    "محاكي الكتابة بالإنجليزية:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = manualTextInput,
                                    onValueChange = { manualTextInput = it },
                                    placeholder = { Text("اكتب هنا ما تود قوله بالإنجليزية...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            if (manualTextInput.isNotBlank() && currentTurn != null) {
                                                viewModel.updateSpokenText(manualTextInput)
                                                viewModel.analyzeSpokenText(currentTurn.text, manualTextInput)
                                                manualTextInput = ""
                                                showManualInput = false
                                            }
                                        }) {
                                            Icon(Icons.Default.Send, contentDescription = "إرسال", tint = Color(0xFF3B82F6))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepProgressBar(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until totalSteps) {
                val isPassed = i <= currentStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isPassed) Color(0xFF3B82F6) else Color(0xFFE5E7EB)
                        )
                )
            }
        }
        
        Text(
            text = "${currentStep + 1} / $totalSteps",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CircularControlButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun DialogueBubble(
    turn: DialogueTurn,
    isActive: Boolean,
    isCompleted: Boolean,
    onPlayAudio: () -> Unit
) {
    val isAI = turn.role == "AI"

    val containerColor = if (isAI) {
        if (isActive) Color(0xFF3B82F6).copy(alpha = 0.08f) else Color(0xFFF3F4F6)
    } else {
        if (isActive) Color(0xFF10B981).copy(alpha = 0.08f) else Color.White
    }

    val textColor = if (isAI) {
        if (isActive) Color(0xFF1E3A8A) else Color(0xFF4B5563)
    } else {
        if (isActive) Color(0xFF065F46) else Color(0xFF1F2937)
    }

    val bubbleAlignment = if (isAI) Alignment.Start else Alignment.End

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = bubbleAlignment
    ) {
        // Speaker label above
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isAI) Arrangement.Start else Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isAI) {
                Icon(
                    Icons.Default.Android,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = Color(0xFF3B82F6)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("المعلم الذكي", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
            } else {
                Text("أنت (المتدرب)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = Color(0xFF10B981)
                )
            }

            if (isActive) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isAI) Color(0xFF3B82F6) else Color(0xFF10B981))
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bubble shape
        Card(
            shape = RoundedCornerShape(
                topStart = if (isAI) 4.dp else 16.dp,
                topEnd = if (isAI) 16.dp else 4.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = if (isActive) BorderStroke(1.5.dp, if (isAI) Color(0xFF3B82F6) else Color(0xFF10B981)) else null,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAI) {
                        IconButton(onClick = onPlayAudio, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "استمع للعبارة", modifier = Modifier.size(16.dp), tint = textColor)
                        }
                    }
                    Text(
                        text = turn.text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = if (isAI) TextAlign.Left else TextAlign.Right,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Translation hint
                Text(
                    text = turn.translation,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )

                if (isActive && turn.pronunciationTip.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.6f))
                            .padding(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Info, contentDescription = "نصيحة", modifier = Modifier.size(14.dp), tint = Color(0xFF3B82F6))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = turn.pronunciationTip,
                                fontSize = 11.sp,
                                color = Color(0xFF1F2937),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
