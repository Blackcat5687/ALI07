package com.example.api

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class GeminiLiveClient(
    private val apiKey: String,
    private val systemInstructionText: String,
    private val listener: LiveClientListener
) {

    interface LiveClientListener {
        fun onStateChanged(state: LiveState)
        fun onError(message: String)
        fun onInterrupted()
    }

    enum class LiveState {
        IDLE,
        CONNECTING,
        LISTENING,
        SPEAKING,
        ERROR
    }

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var isRecording = false
    @Volatile
    private var isPlaying = false

    private val audioQueue = LinkedBlockingQueue<ByteArray>()

    private var recordThread: Thread? = null
    private var playThread: Thread? = null

    private var currentState = LiveState.IDLE
    private var connectionTimeoutTimer: java.util.Timer? = null

    private fun updateState(newState: LiveState) {
        currentState = newState
        listener.onStateChanged(newState)
    }

    private fun startTimeoutTimer() {
        cancelTimeoutTimer()
        connectionTimeoutTimer = java.util.Timer().apply {
            schedule(object : java.util.TimerTask() {
                override fun run() {
                    Log.e("GeminiLiveClient", "Connection timeout reached (12s). This typically indicates that the model name (models/gemini-3.1-flash-live-preview) is deprecated/unsupported or the API key is invalid or lacks proper permission scopes.")
                    listener.onError("تعذر إنشاء الجلسة، قد يكون النموذج أو مفتاح API غير صالح، يرجى المحاولة لاحقاً.")
                    updateState(LiveState.ERROR)
                    stop()
                }
            }, 12000)
        }
    }

    private fun cancelTimeoutTimer() {
        connectionTimeoutTimer?.cancel()
        connectionTimeoutTimer = null
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (apiKey.isEmpty()) {
            listener.onError("مفتاح API الخاص بـ Gemini غير متوفر. يرجى إدخاله في الإعدادات.")
            updateState(LiveState.ERROR)
            return
        }

        updateState(LiveState.CONNECTING)
        startTimeoutTimer()

        // Verified against official documentation at ai.google.dev/api/live on July 18, 2026.
        // Both the '?key=' query parameter and the 'x-goog-api-key' header are supported for upgraded connections.
        val url = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", apiKey)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("GeminiLiveClient", "WebSocket connection opened successfully. Sending setup message.")
                sendSetupMessage(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleWebSocketMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                val httpCode = response?.code
                val bodySnippet = try { response?.body?.string()?.take(300) } catch (e: Exception) { null }
                Log.e(
                    "GeminiLiveClient",
                    "WebSocket failure. httpCode=$httpCode message=${response?.message} body=$bodySnippet",
                    t
                )
                val errMsg = when {
                    httpCode == 401 || httpCode == 403 ->
                        "تم رفض الاتصال (HTTP $httpCode). مفتاح API إما غير صالح أو لا يملك صلاحية الوصول إلى Gemini Live API (BidiGenerateContent). تحقق من المفتاح ومن تفعيل هذه الميزة له."
                    httpCode == 404 ->
                        "لم يتم العثور على النموذج (HTTP 404). قد يكون اسم النموذج غير متاح لحسابك حالياً."
                    httpCode == 429 ->
                        "تم تجاوز الحد المسموح من الطلبات لهذا المفتاح (HTTP 429). حاول لاحقاً أو فعّل الفوترة على مشروعك."
                    httpCode != null ->
                        "فشل الاتصال بالخادم (HTTP $httpCode): ${response?.message ?: bodySnippet ?: "بدون تفاصيل"}"
                    t is java.net.UnknownHostException || t is java.net.SocketTimeoutException ->
                        "خطأ في الاتصال بالشبكة. يرجى التحقق من اتصال الإنترنت."
                    else -> t.localizedMessage ?: "فشل الاتصال بجلسة الذكاء الاصطناعي الحية."
                }
                listener.onError(errMsg)
                updateState(LiveState.ERROR)
                stop()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("GeminiLiveClient", "WebSocket closing: $reason")
                updateState(LiveState.IDLE)
                stop()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("GeminiLiveClient", "WebSocket closed: $reason")
                updateState(LiveState.IDLE)
            }
        })
    }

    private fun sendSetupMessage(ws: WebSocket) {
        try {
            val setupObj = JSONObject()
            val setup = JSONObject()
            setup.put("model", "models/gemini-3.1-flash-live-preview")

            val generationConfig = JSONObject()
            val responseModalities = JSONArray()
            responseModalities.put("AUDIO")
            generationConfig.put("responseModalities", responseModalities)

            val voiceConfig = JSONObject()
            val prebuiltVoiceConfig = JSONObject()
            prebuiltVoiceConfig.put("voiceName", "Aoede") // Dynamic supportive female tutor voice
            voiceConfig.put("prebuiltVoiceConfig", prebuiltVoiceConfig)
            
            val speechConfig = JSONObject()
            speechConfig.put("voiceConfig", voiceConfig)
            generationConfig.put("speechConfig", speechConfig)

            setup.put("generationConfig", generationConfig)

            val systemInstruction = JSONObject()
            val parts = JSONArray()
            val textPart = JSONObject()
            textPart.put("text", systemInstructionText)
            parts.put(textPart)
            systemInstruction.put("parts", parts)
            setup.put("systemInstruction", systemInstruction)

            setupObj.put("setup", setup)

            ws.send(setupObj.toString())
            Log.d("GeminiLiveClient", "Setup configuration sent to Gemini Multimodal Live API.")
        } catch (e: Exception) {
            Log.e("GeminiLiveClient", "Error building setup message", e)
            listener.onError("حدث خطأ أثناء تهيئة إعدادات الجلسة: ${e.localizedMessage}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAudioHardware() {
        try {
            // Setup recording
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize.coerceAtLeast(1024 * 2)
            )

            // Setup playback
            val playSampleRate = 24000
            val playChannelConfig = AudioFormat.CHANNEL_OUT_MONO
            val playAudioFormat = AudioFormat.ENCODING_PCM_16BIT
            val playBufferSize = AudioTrack.getMinBufferSize(playSampleRate, playChannelConfig, playAudioFormat)

            audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(playAudioFormat)
                            .setSampleRate(playSampleRate)
                            .setChannelMask(playChannelConfig)
                            .build()
                    )
                    .setBufferSizeInBytes(playBufferSize.coerceAtLeast(2048 * 2))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    playSampleRate,
                    playChannelConfig,
                    playAudioFormat,
                    playBufferSize.coerceAtLeast(2048 * 2),
                    AudioTrack.MODE_STREAM
                )
            }

            // Start Threads
            isRecording = true
            isPlaying = true

            audioRecord?.startRecording()
            audioTrack?.play()

            recordThread = thread(start = true, name = "gemini-live-record") {
                val pcmBuffer = ByteArray(2048)
                while (isRecording) {
                    val read = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: -1
                    if (read > 0 && isRecording) {
                        val finalBuffer = if (read == pcmBuffer.size) pcmBuffer else pcmBuffer.copyOf(read)
                        sendAudioChunk(finalBuffer)
                    }
                }
            }

            playThread = thread(start = true, name = "gemini-live-play") {
                while (isPlaying) {
                    try {
                        val chunk = audioQueue.take()
                        if (isPlaying) {
                            audioTrack?.write(chunk, 0, chunk.size)
                        }
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("GeminiLiveClient", "Failed to start audio hardware", e)
            listener.onError("فشل في تشغيل نظام الصوت: ${e.localizedMessage}")
            updateState(LiveState.ERROR)
            stop()
        }
    }

    private fun sendAudioChunk(data: ByteArray) {
        val ws = webSocket ?: return
        try {
            val base64Data = Base64.encodeToString(data, Base64.NO_WRAP)

            val realtimeInput = JSONObject()
            val mediaChunks = JSONArray()
            val chunk = JSONObject()
            chunk.put("mimeType", "audio/pcm")
            chunk.put("data", base64Data)
            mediaChunks.put(chunk)
            realtimeInput.put("mediaChunks", mediaChunks)

            val message = JSONObject()
            message.put("realtimeInput", realtimeInput)

            ws.send(message.toString())
        } catch (e: Exception) {
            Log.e("GeminiLiveClient", "Error sending audio chunk", e)
        }
    }

    private fun handleWebSocketMessage(text: String) {
        try {
            Log.d("GeminiLiveClient", "Received raw WebSocket message: $text")
            val root = JSONObject(text)

            // Check for server errors
            if (root.has("error")) {
                val errorObj = root.getJSONObject("error")
                val errMsg = errorObj.optString("message", "Unknown error from server")
                Log.e("GeminiLiveClient", "Server error received: $text")
                listener.onError("خطأ من الخادم: $errMsg")
                updateState(LiveState.ERROR)
                stop()
                return
            }

            // Check for setup confirmation from Gemini Live API
            if (root.has("setupComplete")) {
                Log.d("GeminiLiveClient", "Received setupComplete from server. Starting audio hardware.")
                cancelTimeoutTimer()
                startAudioHardware()
                updateState(LiveState.LISTENING)
                return
            }

            if (root.has("serverContent")) {
                val serverContent = root.getJSONObject("serverContent")

                // Handle interruption (User starts talking while model is speaking)
                if (serverContent.optBoolean("interrupted") == true) {
                    Log.d("GeminiLiveClient", "User interrupted the AI response. Flushing play buffer.")
                    listener.onInterrupted()
                    handleInterruption()
                    updateState(LiveState.LISTENING)
                }

                // Extract AI Audio output parts
                if (serverContent.has("modelTurn")) {
                    val modelTurn = serverContent.getJSONObject("modelTurn")
                    if (modelTurn.has("parts")) {
                        val parts = modelTurn.getJSONArray("parts")
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)
                            if (part.has("text")) {
                                val textContent = part.optString("text")
                                Log.d("GeminiLiveClient", "Model Text Part (Transcription/Response): $textContent")
                            }
                            if (part.has("inlineData")) {
                                val inlineData = part.getJSONObject("inlineData")
                                val base64Data = inlineData.optString("data")
                                if (!base64Data.isNullOrEmpty()) {
                                    val pcmBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                    audioQueue.put(pcmBytes)
                                    
                                    // Change state to speaking because we are receiving speaker outputs
                                    if (currentState != LiveState.SPEAKING) {
                                        updateState(LiveState.SPEAKING)
                                    }
                                }
                            }
                        }
                    }
                }

                // Check if turn is complete
                if (serverContent.optBoolean("turnComplete") == true) {
                    Log.d("GeminiLiveClient", "AI Turn completed.")
                    // Let's launch a helper check to revert state back to listening after some silence or immediately
                    // Since playback is buffered, let's keep state as speaking until queue is near-empty, or transition gracefully
                    thread {
                        while (audioQueue.isNotEmpty() && isPlaying) {
                            Thread.sleep(100)
                        }
                        if (isPlaying && currentState == LiveState.SPEAKING) {
                            updateState(LiveState.LISTENING)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiLiveClient", "Error parsing incoming WebSocket message", e)
        }
    }

    private fun handleInterruption() {
        audioQueue.clear()
        try {
            audioTrack?.stop()
            audioTrack?.flush()
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e("GeminiLiveClient", "Error flushing AudioTrack", e)
        }
    }

    fun stop() {
        cancelTimeoutTimer()
        isRecording = false
        isPlaying = false

        recordThread?.interrupt()
        playThread?.interrupt()
        recordThread = null
        playThread = null

        webSocket?.close(1000, "User ended conversation")
        webSocket = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e("GeminiLiveClient", "Error stopping/releasing AudioRecord", e)
        }
        audioRecord = null

        try {
            audioTrack?.stop()
            audioTrack?.flush()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.e("GeminiLiveClient", "Error stopping/releasing AudioTrack", e)
        }
        audioTrack = null

        audioQueue.clear()
        if (currentState != LiveState.ERROR) {
            updateState(LiveState.IDLE)
        }
    }
}
