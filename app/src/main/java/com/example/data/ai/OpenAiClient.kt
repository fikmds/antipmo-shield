package com.example.data.ai

import com.example.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object OpenAiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Normalizes base URL so that trailing slashes are removed and /v1 is intelligently ensured.
     */
    fun normalizeBaseUrl(raw: String): String {
        var url = raw.trim()
        if (url.isBlank()) return "https://api.openai.com/v1"
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        url = url.removeSuffix("/")
        if (!url.contains("/v1") && !url.contains("/v1beta") && !url.contains("/v2") && !url.contains("/api")) {
            url = "$url/v1"
        }
        return url
    }

    /**
     * Fetches available models from OpenAI-compatible endpoint: GET {baseUrl}/models
     */
    suspend fun fetchModels(baseUrl: String, apiKey: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = normalizeBaseUrl(baseUrl)
            val targetUrl = "$cleanUrl/models"

            val requestBuilder = Request.Builder()
                .url(targetUrl)
                .addHeader("Accept", "application/json")
                .addHeader("HTTP-Referer", "https://antipmo.app")
                .addHeader("X-Title", "Anti PMO Shield")
                .get()

            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${apiKey.trim()}")
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val friendlyError = parseHttpError(response.code, bodyString)
                    return@withContext Result.failure(Exception(friendlyError))
                }

                val json = JSONObject(bodyString)
                val dataArray = json.optJSONArray("data") ?: JSONArray()
                val modelsList = mutableListOf<String>()
                for (i in 0 until dataArray.length()) {
                    val modelObj = dataArray.optJSONObject(i)
                    val id = modelObj?.optString("id") ?: ""
                    if (id.isNotBlank()) {
                        modelsList.add(id)
                    }
                }

                val finalModels = if (modelsList.isNotEmpty()) {
                    modelsList.sorted()
                } else {
                    listOf(
                        "gpt-4o-mini",
                        "gpt-4o",
                        "gpt-3.5-turbo",
                        "deepseek-chat",
                        "deepseek-reasoner",
                        "claude-3-5-sonnet",
                        "llama-3.1-70b-versatile"
                    )
                }

                Result.success(finalModels)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Koneksi gagal: ${e.localizedMessage}"))
        }
    }

    /**
     * Sends a chat completion request to OpenAI-compatible endpoint: POST {baseUrl}/chat/completions
     */
    suspend fun sendChatMessage(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessage>,
        systemPrompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = normalizeBaseUrl(baseUrl)
            val targetUrl = "$cleanUrl/chat/completions"

            val jsonArray = JSONArray()

            // System prompt
            if (systemPrompt.isNotBlank()) {
                val sysObj = JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                }
                jsonArray.put(sysObj)
            }

            // Chat conversation history (last 10 messages for token efficiency)
            val history = messages.takeLast(10)
            for (msg in history) {
                val msgObj = JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                }
                jsonArray.put(msgObj)
            }

            val requestJson = JSONObject().apply {
                put("model", model.ifBlank { "gpt-4o-mini" })
                put("messages", jsonArray)
                put("temperature", 0.7)
            }

            val body = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val requestBuilder = Request.Builder()
                .url(targetUrl)
                .addHeader("Accept", "application/json")
                .addHeader("HTTP-Referer", "https://antipmo.app")
                .addHeader("X-Title", "Anti PMO Shield")
                .post(body)

            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${apiKey.trim()}")
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val friendlyError = parseHttpError(response.code, bodyString)
                    return@withContext Result.failure(Exception(friendlyError))
                }

                val json = JSONObject(bodyString)
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    for (i in 0 until choices.length()) {
                        val choiceObj = choices.optJSONObject(i) ?: continue
                        val extracted = extractContentFromChoice(choiceObj)
                        if (extracted.isNotBlank()) {
                            return@withContext Result.success(extracted)
                        }
                    }
                }

                // Fallback to top-level text/response if any
                val topLevelText = json.optString("text").ifBlank { json.optString("response") }
                if (topLevelText.isNotBlank()) {
                    return@withContext Result.success(topLevelText)
                }

                Result.failure(Exception("Jawaban AI kosong dari server (Format respons tidak dikenali)."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Koneksi chat gagal: ${e.localizedMessage ?: "Timeout / Jaringan terputus"}"))
        }
    }

    private fun extractContentFromChoice(choice: JSONObject): String {
        val messageObj = choice.optJSONObject("message")
        if (messageObj != null) {
            val contentAny = messageObj.opt("content")
            if (contentAny is String && contentAny.isNotBlank()) {
                return contentAny
            } else if (contentAny is JSONArray) {
                val sb = StringBuilder()
                for (i in 0 until contentAny.length()) {
                    val part = contentAny.optJSONObject(i)
                    if (part != null) {
                        val text = part.optString("text")
                        if (text.isNotBlank()) sb.append(text)
                    } else {
                        val str = contentAny.optString(i)
                        if (str.isNotBlank()) sb.append(str)
                    }
                }
                if (sb.isNotBlank()) return sb.toString()
            }
            val reasoning = messageObj.optString("reasoning_content")
            if (reasoning.isNotBlank()) {
                return reasoning
            }
        }
        val directText = choice.optString("text")
        if (directText.isNotBlank()) {
            return directText
        }
        return ""
    }

    private fun parseHttpError(code: Int, body: String): String {
        val serverMsg = try {
            val json = JSONObject(body)
            val errObj = json.optJSONObject("error")
            errObj?.optString("message") ?: json.optString("message")
        } catch (_: Exception) {
            ""
        }

        return when (code) {
            401 -> "API Key tidak valid atau belum diisi (401 Unauthorized). Silakan cek di pengaturan API."
            403 -> "Akses ditolak oleh server AI (403 Forbidden)."
            404 -> "Endpoint URL atau Model '${serverMsg.ifBlank { "tidak ditemukan" }}' (404 Not Found). Periksa Base URL dan Nama Model."
            429 -> "Batas kuota atau rate limit terlampaui (429 Too Many Requests). Cek saldo/kuota API Anda."
            500, 502, 503 -> "Server AI sedang sibuk/mengalami gangguan (HTTP $code). Coba beberapa saat lagi."
            else -> if (serverMsg.isNotBlank()) "Error AI ($code): $serverMsg" else "HTTP $code: ${body.take(100)}"
        }
    }
}
