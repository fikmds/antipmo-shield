package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AntiPmoPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("anti_pmo_prefs", Context.MODE_PRIVATE)

    private val _filterAdult = MutableStateFlow(prefs.getBoolean(KEY_FILTER_ADULT, true))
    val filterAdult: StateFlow<Boolean> = _filterAdult.asStateFlow()

    private val _filterTerabox = MutableStateFlow(prefs.getBoolean(KEY_FILTER_TERABOX, true))
    val filterTerabox: StateFlow<Boolean> = _filterTerabox.asStateFlow()

    private val _filterUc = MutableStateFlow(prefs.getBoolean(KEY_FILTER_UC, true))
    val filterUc: StateFlow<Boolean> = _filterUc.asStateFlow()

    private val _browserOnlyMode = MutableStateFlow(prefs.getBoolean(KEY_BROWSER_ONLY, true))
    val browserOnlyMode: StateFlow<Boolean> = _browserOnlyMode.asStateFlow()

    private val _whitelistedWords = MutableStateFlow(
        prefs.getStringSet(KEY_WHITELIST_WORDS, DEFAULT_WHITELIST_WORDS)?.toSet() ?: DEFAULT_WHITELIST_WORDS
    )
    val whitelistedWords: StateFlow<Set<String>> = _whitelistedWords.asStateFlow()

    private val _whitelistedApps = MutableStateFlow(
        prefs.getStringSet(KEY_WHITELIST_APPS, DEFAULT_WHITELIST_APPS)?.toSet() ?: DEFAULT_WHITELIST_APPS
    )
    val whitelistedApps: StateFlow<Set<String>> = _whitelistedApps.asStateFlow()

    // AI Settings
    private val _aiBaseUrl = MutableStateFlow(prefs.getString(KEY_AI_BASE_URL, "https://api.openai.com/v1") ?: "https://api.openai.com/v1")
    val aiBaseUrl: StateFlow<String> = _aiBaseUrl.asStateFlow()

    private val _aiApiKey = MutableStateFlow(prefs.getString(KEY_AI_API_KEY, "") ?: "")
    val aiApiKey: StateFlow<String> = _aiApiKey.asStateFlow()

    private val _aiModel = MutableStateFlow(prefs.getString(KEY_AI_MODEL, "gpt-4o-mini") ?: "gpt-4o-mini")
    val aiModel: StateFlow<String> = _aiModel.asStateFlow()

    private val _aiSystemPrompt = MutableStateFlow(
        prefs.getString(
            KEY_AI_SYSTEM_PROMPT,
            "Kamu adalah AI Anti-PMO Recovery Coach & Mental Armor. Berikan jawaban yang tegas, memotivasi, berbasis sains dopamin otak, penuh empati namun tidak mentolerir alasan relapse. Gunakan bahasa Indonesia yang santun tapi tegas dan membangun mental baja."
        ) ?: ""
    )
    val aiSystemPrompt: StateFlow<String> = _aiSystemPrompt.asStateFlow()

    fun setFilterAdult(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FILTER_ADULT, enabled).apply()
        _filterAdult.value = enabled
    }

    fun setFilterTerabox(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FILTER_TERABOX, enabled).apply()
        _filterTerabox.value = enabled
    }

    fun setFilterUc(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FILTER_UC, enabled).apply()
        _filterUc.value = enabled
    }

    fun setBrowserOnlyMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BROWSER_ONLY, enabled).apply()
        _browserOnlyMode.value = enabled
    }

    fun addWhitelistedWord(word: String) {
        val trimmed = word.trim().lowercase()
        if (trimmed.isNotBlank()) {
            val updated = _whitelistedWords.value + trimmed
            prefs.edit().putStringSet(KEY_WHITELIST_WORDS, updated).apply()
            _whitelistedWords.value = updated
        }
    }

    fun removeWhitelistedWord(word: String) {
        val updated = _whitelistedWords.value - word.lowercase()
        prefs.edit().putStringSet(KEY_WHITELIST_WORDS, updated).apply()
        _whitelistedWords.value = updated
    }

    fun addWhitelistedApp(packageName: String) {
        val trimmed = packageName.trim()
        if (trimmed.isNotBlank()) {
            val updated = _whitelistedApps.value + trimmed
            prefs.edit().putStringSet(KEY_WHITELIST_APPS, updated).apply()
            _whitelistedApps.value = updated
        }
    }

    fun removeWhitelistedApp(packageName: String) {
        val updated = _whitelistedApps.value - packageName
        prefs.edit().putStringSet(KEY_WHITELIST_APPS, updated).apply()
        _whitelistedApps.value = updated
    }

    fun saveAiConfig(baseUrl: String, apiKey: String, model: String, systemPrompt: String) {
        val cleanBaseUrl = baseUrl.trim().removeSuffix("/")
        val cleanModel = model.trim().ifBlank { "gpt-4o-mini" }
        prefs.edit()
            .putString(KEY_AI_BASE_URL, cleanBaseUrl)
            .putString(KEY_AI_API_KEY, apiKey.trim())
            .putString(KEY_AI_MODEL, cleanModel)
            .putString(KEY_AI_SYSTEM_PROMPT, systemPrompt.trim())
            .apply()
        _aiBaseUrl.value = cleanBaseUrl
        _aiApiKey.value = apiKey.trim()
        _aiModel.value = cleanModel
        _aiSystemPrompt.value = systemPrompt.trim()
    }

    companion object {
        private const val KEY_FILTER_ADULT = "pref_filter_adult"
        private const val KEY_FILTER_TERABOX = "pref_filter_terabox"
        private const val KEY_FILTER_UC = "pref_filter_uc"
        private const val KEY_BROWSER_ONLY = "pref_browser_only"
        private const val KEY_WHITELIST_WORDS = "pref_whitelist_words"
        private const val KEY_WHITELIST_APPS = "pref_whitelist_apps"

        private const val KEY_AI_BASE_URL = "pref_ai_base_url"
        private const val KEY_AI_API_KEY = "pref_ai_api_key"
        private const val KEY_AI_MODEL = "pref_ai_model"
        private const val KEY_AI_SYSTEM_PROMPT = "pref_ai_system_prompt"

        val DEFAULT_WHITELIST_WORDS = setOf(
            "javascript",
            "java development",
            "broccoli",
            "open box",
            "unboxing",
            "vcs git",
            "version control",
            "pornography addiction recovery",
            "anti pmo",
            "berhenti bokep"
        )

        val DEFAULT_WHITELIST_APPS = setOf(
            "com.whatsapp",
            "org.telegram.messenger",
            "com.google.android.apps.docs",
            "com.google.android.keep",
            "com.google.android.inputmethod.latin",
            "com.touchtype.swiftkey",
            "com.samsung.android.honeyboard"
        )
    }
}
