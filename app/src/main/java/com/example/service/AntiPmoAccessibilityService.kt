package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.db.AntiPmoDatabase
import com.example.data.model.BlockedLog
import com.example.data.model.CustomRule
import com.example.data.preferences.AntiPmoPreferences
import com.example.ui.BlockedAlertActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AntiPmoAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: AntiPmoDatabase
    private lateinit var preferences: AntiPmoPreferences

    private var cachedCustomRules: List<CustomRule> = emptyList()

    private var lastInterceptTime: Long = 0
    private var lastInterceptKey: String = ""

    companion object {
        private const val TAG = "AntiPmoService"
        // Cooldown between repeated triggers for the EXACT same pattern and app (prevents event flood)
        private const val MIN_INTERCEPT_INTERVAL_MS = 800L

        @Volatile
        var isAlertCurrentlyShowing: Boolean = false

        @Volatile
        var lastDismissTimeMs: Long = 0
    }

    override fun onCreate() {
        super.onCreate()
        database = AntiPmoDatabase.getInstance(applicationContext)
        preferences = AntiPmoPreferences(applicationContext)

        // Observe custom rules from DB
        serviceScope.launch {
            database.customRuleDao().getAllRules().collect { rules ->
                cachedCustomRules = rules
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC or AccessibilityServiceInfo.FEEDBACK_VISUAL
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 50
        }
        serviceInfo = info
        Log.i(TAG, "AntiPmoAccessibilityService connected and actively protecting.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        // 1. Never intercept our own app
        if (packageName == this.packageName) return

        // 2. Short snooze protection after user deliberately dismissed the alert (max 2.5 seconds)
        val now = SystemClock.elapsedRealtime()
        if (now - lastDismissTimeMs < 2500L) return

        // 3. Check App Whitelist
        val whitelistedApps = preferences.whitelistedApps.value
        if (whitelistedApps.contains(packageName)) return

        // 4. If Browser-Only Mode is active, check against browser list
        val browserOnly = preferences.browserOnlyMode.value
        if (browserOnly && !AntiPmoFilterEngine.KNOWN_BROWSER_PACKAGES.contains(packageName)) {
            return
        }

        // 5. Skip virtual keyboard typing noise if text is short
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
        ) {
            val text = event.text?.joinToString(" ") ?: ""
            if (text.length < 3) return
        }

        // 6. Collect text safely from event and node hierarchy
        try {
            val collectedTexts = mutableListOf<String>()

            // Event-level texts
            event.text?.forEach { t ->
                if (!t.isNullOrBlank()) collectedTexts.add(t.toString())
            }
            event.contentDescription?.let { cd ->
                if (cd.isNotBlank()) collectedTexts.add(cd.toString())
            }

            // Window-level node inspection
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                extractTextFromNodeSafely(rootNode, collectedTexts, maxDepth = 12)
            }

            if (collectedTexts.isEmpty()) return

            val aggregatedText = collectedTexts.joinToString(" ")
            if (aggregatedText.isBlank()) return

            // 7. Evaluate against filter engine
            val match = AntiPmoFilterEngine.evaluate(
                input = aggregatedText,
                customRules = cachedCustomRules,
                filterAdult = preferences.filterAdult.value,
                filterTerabox = preferences.filterTerabox.value,
                filterUc = preferences.filterUc.value,
                whitelistedWords = preferences.whitelistedWords.value
            )

            if (match.matched) {
                handleMatchIntercept(
                    targetApp = packageName,
                    pattern = match.pattern,
                    category = match.category,
                    reason = match.reason,
                    snippet = aggregatedText.take(150)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in accessibility scan", e)
        }
    }

    private fun handleMatchIntercept(
        targetApp: String,
        pattern: String,
        category: String,
        reason: String,
        snippet: String
    ) {
        val now = SystemClock.elapsedRealtime()
        val interceptKey = "$targetApp|$pattern"

        // Anti-flood debounce: if exact same pattern on same app was triggered in last 800ms, skip duplicate
        if (interceptKey == lastInterceptKey && (now - lastInterceptTime) < MIN_INTERCEPT_INTERVAL_MS) {
            return
        }

        lastInterceptTime = now
        lastInterceptKey = interceptKey

        Log.w(TAG, "PMO SHIELD INTERCEPT: $category - $reason in $targetApp")

        // Trigger brief haptic warning
        triggerVibration()

        // Record log into Room DB
        serviceScope.launch {
            try {
                val log = BlockedLog(
                    triggerSource = targetApp,
                    matchedPattern = pattern,
                    category = category,
                    targetUrlOrText = snippet,
                    packageName = targetApp
                )
                database.blockedLogDao().insertLog(log)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save block log", e)
            }
        }

        // Direct Auto-Open Intercept Fullscreen Alert Pop-up
        try {
            val intent = Intent(this, BlockedAlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                putExtra(BlockedAlertActivity.EXTRA_CATEGORY, category)
                putExtra(BlockedAlertActivity.EXTRA_REASON, reason)
                putExtra(BlockedAlertActivity.EXTRA_SOURCE_APP, targetApp)
                putExtra(BlockedAlertActivity.EXTRA_PATTERN, pattern)
                putExtra(BlockedAlertActivity.EXTRA_TRIGGER, pattern)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start BlockedAlertActivity", e)
        }
    }

    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v?.vibrate(200)
                }
            }
        } catch (_: Exception) {
            // Ignore vibration permission or hardware exceptions
        }
    }

    private fun extractTextFromNodeSafely(
        node: AccessibilityNodeInfo?,
        results: MutableList<String>,
        maxDepth: Int
    ) {
        if (node == null || maxDepth <= 0 || results.size > 100) return

        try {
            node.text?.toString()?.let {
                if (it.isNotBlank()) results.add(it)
            }
            node.contentDescription?.toString()?.let {
                if (it.isNotBlank()) results.add(it)
            }
            node.viewIdResourceName?.let { idName ->
                // Check address bar or search bar
                if (idName.contains("url", ignoreCase = true) ||
                    idName.contains("address", ignoreCase = true) ||
                    idName.contains("search", ignoreCase = true)
                ) {
                    node.text?.toString()?.let { text -> if (text.isNotBlank()) results.add(text) }
                }
            }

            val count = node.childCount
            for (i in 0 until count) {
                val child = node.getChild(i) ?: continue
                extractTextFromNodeSafely(child, results, maxDepth - 1)
            }
        } catch (_: Exception) {
            // Safety catch against recycled node exceptions in rapid UI transitions
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
