package com.example.api

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

// --- Gemini Request/Response Models ---

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = "application/json",
    @Json(name = "temperature") val temperature: Double? = 0.2
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = GeminiGenerationConfig(),
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

// --- App Specific Response Model ---

@JsonClass(generateAdapter = true)
data class PronunciationAnalysis(
    @Json(name = "score") val score: Int,
    @Json(name = "feedback") val feedback: String,
    @Json(name = "mispronouncedWords") val mispronouncedWords: List<MispronouncedWord>
)

@JsonClass(generateAdapter = true)
data class MispronouncedWord(
    @Json(name = "word") val word: String,
    @Json(name = "tip") val tip: String
)

// --- Retrofit Interface ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @retrofit2.http.Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// --- API Client ---

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Compares the user's spoken sentence against the target sentence
     * and returns a detailed pronunciation score and Arabic correction tips.
     */
    suspend fun analyzePronunciation(target: String, spoken: String, customApiKey: String = ""): PronunciationAnalysis {
        val apiKey = if (customApiKey.isNotBlank()) customApiKey else BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            return PronunciationAnalysis(
                score = 0,
                feedback = "مفتاح API الخاص بـ Gemini غير متوفر. يرجى تهيئته في لوحة الإعدادات أو لوحة Secrets.",
                mispronouncedWords = emptyList()
            )
     }

        val promptText = """
            Target English sentence: "$target"
            Spoken English text: "$spoken"
            
            Compare the spoken text with the target sentence. Find any mispronounced, skipped, or wrongly added words.
            Evaluate the pronunciation score from 0 to 100 based on accuracy (near-perfect = 100, slightly off = 80-90, very different = 40-70).
            Provide helpful, encouraging advice in Arabic (feedback).
            List all mispronounced or skipped words and provide specific Arabic tips on how to pronounce each word correctly.
            
            Format your response exactly as a JSON object matching this schema:
            {
              "score": <integer from 0 to 100>,
              "feedback": "<encouraging Arabic feedback>",
              "mispronouncedWords": [
                {
                  "word": "<mispronounced word>",
                  "tip": "<Arabic phonetic/pronunciation tip on how to say it right>"
                }
              ]
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(promptText)))),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart("You are an expert English accent coach who helps native Arabic speakers perfect their pronunciation through fun, supportive, and precise audio corrections."))
            )
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                // Sanitize JSON text in case the model wrapped it in Markdown code blocks
                val cleanJson = if (jsonText.contains("```json")) {
                    jsonText.substringAfter("```json").substringBefore("```").trim()
                } else if (jsonText.contains("```")) {
                    jsonText.substringAfter("```").substringBefore("```").trim()
                } else {
                    jsonText.trim()
                }
                
                val adapter = moshi.adapter(PronunciationAnalysis::class.java)
                adapter.fromJson(cleanJson) ?: throw Exception("Moshi failed to parse response JSON")
            } else {
                throw Exception("Received empty response from Gemini API")
            }
        } catch (e: Exception) {
            Log.e("GeminiClient", "Failed to analyze pronunciation", e)
            val errorMessage = when (e) {
                is java.io.IOException -> {
                    "لا يوجد اتصال بالإنترنت. يرجى التحقق من اتصال شبكتك والمحاولة مرة أخرى."
                }
                is retrofit2.HttpException -> {
                    val code = e.code()
                    val errorBody = try {
                        e.response()?.errorBody()?.string()?.take(300)
                    } catch (ex: Exception) {
                        null
                    }
                    val bodyText = if (!errorBody.isNullOrBlank()) "\nالرد من الخادم: $errorBody" else ""
                    if (code == 400 || code == 403 || code == 401) {
                        "مفتاح API الخاص بـ Gemini غير صالح أو تم رفضه من قِبل Google (HTTP $code). يرجى التحقق من المفتاح في الإعدادات.$bodyText"
                    } else {
                        "حدث خطأ في خادم Gemini API (رمز الخطأ HTTP $code).$bodyText"
                    }
                }
                else -> {
                    val msg = e.localizedMessage ?: "خطأ غير معروف"
                    if (msg.contains("API_KEY", ignoreCase = true) || msg.contains("API key", ignoreCase = true)) {
                        "مفتاح API الخاص بـ Gemini غير صالح أو منتهي الصلاحية. يرجى تحديثه في شاشة الإعدادات."
                    } else {
                        "حدث خطأ غير متوقع أثناء الاتصال بالذكاء الاصطناعي: $msg"
                    }
                }
            }

            // Fallback similarity calculator in case of network/key errors
            val calculatedScore = calculateBasicSimilarity(target, spoken)
            PronunciationAnalysis(
                score = calculatedScore,
                feedback = "$errorMessage\n\n(لقد قمنا بحساب نسبة تطابق الكلمات تلقائياً بمعدل $calculatedScore% لتستمر في التدريب!)",
                mispronouncedWords = emptyList()
            )
        }
    }

    // Fallback similarity calculator in case of network errors
    private fun calculateBasicSimilarity(target: String, spoken: String): Int {
        val targetWords = target.lowercase().replace(Regex("[^a-zA-Z\\s]"), "").split("\\s+".toRegex()).toSet()
        val spokenWords = spoken.lowercase().replace(Regex("[^a-zA-Z\\s]"), "").split("\\s+".toRegex()).toSet()
        if (targetWords.isEmpty()) return 100
        val commonWords = targetWords.intersect(spokenWords)
        return ((commonWords.size.toFloat() / targetWords.size.toFloat()) * 100).toInt()
    }
}
