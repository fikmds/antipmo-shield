package com.example.ui.viewmodel

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.OpenAiClient
import com.example.data.db.AntiPmoDatabase
import com.example.data.model.BlockedLog
import com.example.data.model.ChatMessage
import com.example.data.model.CustomRule
import com.example.data.preferences.AntiPmoPreferences
import com.example.data.repository.AntiPmoRepository
import com.example.service.AntiPmoAccessibilityService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StreakInfo(
    val days: Int = 0,
    val hours: Int = 0,
    val minutes: Int = 0,
    val seconds: Int = 0,
    val totalSeconds: Long = 0,
    val brainRecoveryStage: String = "Tahap 1: Detoksifikasi Awal",
    val brainRecoveryProgress: Float = 0.1f,
    val stageDescription: String = "Reseptor dopamin mulai beristirahat dari overstimulasi."
)

val DEFAULT_HARSH_MOTIVATIONAL_QUOTES = listOf(
    "Disiplin adalah memilih antara apa yang kamu inginkan sekarang dengan apa yang paling kamu inginkan nanti.",
    "Otakmu butuh waktu untuk membersihkan dan me-reset reseptor dopamin. Bertahanlah!",
    "Kemenangan terbesar adalah menaklukkan dorongan impulsif diri sendiri.",
    "Dopamin murah menghancurkan motivasi, fokus, dan harga diri jangka panjangmu.",
    "Setiap kali kamu menolak godaan, prefrontal cortex otakmu bertambah kuat.",
    "Jadilah orang yang memegang kendali atas tubuh dan pikiran sendiri."
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AntiPmoDatabase.getInstance(application)
    private val repository = AntiPmoRepository(
        database.blockedLogDao(),
        database.customRuleDao(),
        database.recoveryDao()
    )
    val preferences = AntiPmoPreferences(application)

    // Accessibility & Overlay Permissions Status
    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    private val _isOverlayGranted = MutableStateFlow(false)
    val isOverlayGranted: StateFlow<Boolean> = _isOverlayGranted.asStateFlow()

    // Logs & Rules
    val allLogs: StateFlow<List<BlockedLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedLogs: StateFlow<List<BlockedLog>> = allLogs

    val blockedCount: StateFlow<Int> = repository.blockedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val customRules: StateFlow<List<CustomRule>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Streak & Recovery Profile
    val streakInfo: StateFlow<StreakInfo> = repository.recoveryProfile.map { profile ->
        val startTs = profile?.streakStartTimestamp ?: System.currentTimeMillis()
        val diffMs = (System.currentTimeMillis() - startTs).coerceAtLeast(0L)
        val totalSec = diffMs / 1000
        val days = (totalSec / 86400).toInt()
        val hours = ((totalSec % 86400) / 3600).toInt()
        val minutes = ((totalSec % 3600) / 60).toInt()
        val seconds = (totalSec % 60).toInt()

        val stage: String
        val progress: Float
        val desc: String

        when {
            days < 3 -> {
                stage = "Tahap 1: Detoksifikasi Awal (Hari 0-3)"
                progress = ((days * 24 + hours) / 72f).coerceIn(0.05f, 1f)
                desc = "Gejala putus zat & urge paling tinggi. Reseptor dopamin mulai beristirahat."
            }
            days < 7 -> {
                stage = "Tahap 2: Stabilisasi Neurokimia (Hari 4-7)"
                progress = ((days - 3) / 4f).coerceIn(0.1f, 1f)
                desc = "Dorongan impulsif mulai berkurang. Kabut otak (brain fog) perlahan menipis."
            }
            days < 21 -> {
                stage = "Tahap 3: Pemulihan Prefrontal Cortex (Hari 8-21)"
                progress = ((days - 7) / 14f).coerceIn(0.1f, 1f)
                desc = "Kendali diri dan fokus meningkat drastis. Motivasi alami kembali pulih."
            }
            days < 90 -> {
                stage = "Tahap 4: Neuroplastisitas & Kebiasaan Baru (Hari 22-90)"
                progress = ((days - 21) / 69f).coerceIn(0.1f, 1f)
                desc = "Jalur saraf lama melemah, jalur kebiasaan sehat baru terbentuk kuat."
            }
            else -> {
                stage = "Tahap 5: Rewired Master (90+ Hari)"
                progress = 1.0f
                desc = "Otak telah pulih total dari kecanduan pornografi! Pertahankan gaya hidup ini."
            }
        }

        StreakInfo(
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            totalSeconds = totalSec,
            brainRecoveryStage = stage,
            brainRecoveryProgress = progress,
            stageDescription = desc
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StreakInfo()
    )

    private val _currentQuoteIndex = MutableStateFlow(0)
    val currentQuoteIndex: StateFlow<Int> = _currentQuoteIndex.asStateFlow()

    // Emergency Breathing State
    private val _isEmergencyActive = MutableStateFlow(false)
    val isEmergencyActive: StateFlow<Boolean> = _isEmergencyActive.asStateFlow()

    private val _breathingPhase = MutableStateFlow("Tarik Napas")
    val breathingPhase: StateFlow<String> = _breathingPhase.asStateFlow()

    private val _breathingSecondsLeft = MutableStateFlow(60)
    val breathingSecondsLeft: StateFlow<Int> = _breathingSecondsLeft.asStateFlow()

    private var emergencyJob: Job? = null

    val HARSH_MOTIVATIONAL_QUOTES: List<String> = DEFAULT_HARSH_MOTIVATIONAL_QUOTES
    val motivationalQuotes: List<String> = DEFAULT_HARSH_MOTIVATIONAL_QUOTES
    val filterAdult: StateFlow<Boolean> = preferences.filterAdult
    val filterTerabox: StateFlow<Boolean> = preferences.filterTerabox
    val filterUc: StateFlow<Boolean> = preferences.filterUc
    val browserOnlyMode: StateFlow<Boolean> = preferences.browserOnlyMode
    val whitelistedWords: StateFlow<Set<String>> = preferences.whitelistedWords
    val whitelistedApps: StateFlow<Set<String>> = preferences.whitelistedApps

    // AI Configuration & State
    val aiBaseUrl: StateFlow<String> = preferences.aiBaseUrl
    val aiApiKey: StateFlow<String> = preferences.aiApiKey
    val aiModel: StateFlow<String> = preferences.aiModel
    val aiSystemPrompt: StateFlow<String> = preferences.aiSystemPrompt

    private val _availableModels = MutableStateFlow<List<String>>(
        listOf(
            "gpt-4o-mini",
            "gpt-4o",
            "gpt-3.5-turbo",
            "deepseek-chat",
            "claude-3-5-sonnet",
            "llama-3.1-70b-versatile"
        )
    )
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

    private val _isFetchingModels = MutableStateFlow(false)
    val isFetchingModels: StateFlow<Boolean> = _isFetchingModels.asStateFlow()

    private val _fetchModelsError = MutableStateFlow<String?>(null)
    val fetchModelsError: StateFlow<String?> = _fetchModelsError.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = "assistant",
                content = "Halo Pejuang! Saya adalah AI Anti-PMO Recovery Coach Anda. Saat Anda merasakan godaan (urge), stres, atau butuh strategi perlindungan mental, segera bicarakan di sini. Saya siap mendampingi Anda 24/7!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiSending = MutableStateFlow(false)
    val isAiSending: StateFlow<Boolean> = _isAiSending.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    init {
        checkPermissions()
        viewModelScope.launch {
            repository.seedInitialRulesIfNeeded()
        }
    }

    fun checkPermissions() {
        val app = getApplication<Application>()
        val am = app.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        val enabledServices = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        val serviceName = AntiPmoAccessibilityService::class.java.name
        val isAccRunning = enabledServices?.any {
            it.resolveInfo.serviceInfo.name == serviceName ||
                    it.resolveInfo.serviceInfo.packageName == app.packageName
        } ?: false

        _isAccessibilityEnabled.value = isAccRunning

        val canDraw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(app)
        } else {
            true
        }
        _isOverlayGranted.value = canDraw
    }

    fun nextQuote() {
        _currentQuoteIndex.value = (_currentQuoteIndex.value + 1) % HARSH_MOTIVATIONAL_QUOTES.size
    }

    fun startEmergencyBreathing() {
        if (_isEmergencyActive.value) return
        _isEmergencyActive.value = true
        _breathingSecondsLeft.value = 60

        emergencyJob?.cancel()
        emergencyJob = viewModelScope.launch {
            var cycle = 0
            while (_isEmergencyActive.value && _breathingSecondsLeft.value > 0) {
                when (cycle % 3) {
                    0 -> _breathingPhase.value = "Inhale (Tarik Napas Dalam 4s)"
                    1 -> _breathingPhase.value = "Hold (Tahan & Sadari Kendali 4s)"
                    2 -> _breathingPhase.value = "Exhale (Hembuskan Perlahan 4s)"
                }
                delay(4000L)
                _breathingSecondsLeft.value = (_breathingSecondsLeft.value - 4).coerceAtLeast(0)
                cycle++
            }
            _isEmergencyActive.value = false
            _breathingPhase.value = "Selesai! Otak Anda Sudah Lebih Tenang."
        }
    }

    fun stopEmergencyBreathing() {
        _isEmergencyActive.value = false
        emergencyJob?.cancel()
    }

    fun setFilterAdult(enabled: Boolean) = preferences.setFilterAdult(enabled)
    fun setFilterTerabox(enabled: Boolean) = preferences.setFilterTerabox(enabled)
    fun setFilterUc(enabled: Boolean) = preferences.setFilterUc(enabled)
    fun setBrowserOnlyMode(enabled: Boolean) = preferences.setBrowserOnlyMode(enabled)

    fun addWhitelistedWord(word: String) = preferences.addWhitelistedWord(word)
    fun removeWhitelistedWord(word: String) = preferences.removeWhitelistedWord(word)
    fun addWhitelistedApp(packageName: String) = preferences.addWhitelistedApp(packageName)
    fun removeWhitelistedApp(packageName: String) = preferences.removeWhitelistedApp(packageName)

    fun resetStreak() {
        viewModelScope.launch {
            repository.resetStreak()
        }
    }

    fun insertCustomRule(pattern: String, category: String, isRegex: Boolean, description: String = "") {
        viewModelScope.launch {
            repository.insertCustomRule(
                pattern = pattern.trim(),
                isRegex = isRegex,
                category = category,
                description = description.trim()
            )
        }
    }

    fun toggleRule(rule: CustomRule, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleRule(rule, isEnabled)
        }
    }

    fun deleteRule(rule: CustomRule) {
        viewModelScope.launch {
            repository.deleteRule(rule.id)
        }
    }

    fun deleteRule(ruleId: Long) {
        viewModelScope.launch {
            repository.deleteRule(ruleId)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAllLogs()
        }
    }

    // AI Coach Functions
    fun saveAiConfig(baseUrl: String, apiKey: String, model: String, systemPrompt: String) {
        preferences.saveAiConfig(baseUrl, apiKey, model, systemPrompt)
    }

    fun fetchModelsFromApi(baseUrl: String, apiKey: String) {
        viewModelScope.launch {
            _isFetchingModels.value = true
            _fetchModelsError.value = null
            val result = OpenAiClient.fetchModels(baseUrl, apiKey)
            result.onSuccess { models ->
                if (models.isNotEmpty()) {
                    _availableModels.value = models
                }
                _isFetchingModels.value = false
            }.onFailure { error ->
                _fetchModelsError.value = error.localizedMessage ?: "Gagal memuat daftar model"
                _isFetchingModels.value = false
            }
        }
    }

    fun sendUserChatMessage(prompt: String) {
        val userText = prompt.trim()
        if (userText.isBlank() || _isAiSending.value) return

        val userMessage = ChatMessage(role = "user", content = userText)
        val currentHistory = _chatMessages.value + userMessage
        _chatMessages.value = currentHistory
        _chatError.value = null
        _isAiSending.value = true

        viewModelScope.launch {
            val result = OpenAiClient.sendChatMessage(
                baseUrl = preferences.aiBaseUrl.value,
                apiKey = preferences.aiApiKey.value,
                model = preferences.aiModel.value,
                messages = currentHistory,
                systemPrompt = preferences.aiSystemPrompt.value
            )

            result.onSuccess { responseText ->
                val assistantMessage = ChatMessage(role = "assistant", content = responseText)
                _chatMessages.value = _chatMessages.value + assistantMessage
                _isAiSending.value = false
            }.onFailure { err ->
                _chatError.value = err.localizedMessage ?: "Terjadi kesalahan saat memproses jawaban AI"
                _isAiSending.value = false
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                role = "assistant",
                content = "Riwayat chat telah dibersihkan. Ceritakan kondisi atau godaan yang Anda alami, AI Coach siap membantu!"
            )
        )
        _chatError.value = null
    }
}
