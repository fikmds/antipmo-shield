package com.example.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.MainActivity
import com.example.service.AntiPmoAccessibilityService
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class BlockedAlertActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_REASON = "extra_reason"
        const val EXTRA_SOURCE_APP = "extra_source_app"
        const val EXTRA_PATTERN = "extra_pattern"
        const val EXTRA_TRIGGER = "extra_trigger"
    }

    private var currentCategory = mutableStateOf("PMO_ALERT")
    private var currentReason = mutableStateOf("Akses konten dicegat oleh sistem Anti-PMO Shield")
    private var currentSourceApp = mutableStateOf("Browser/App")
    private var currentPattern = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        updateFromIntent(intent)
        vibrateWarning()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BlockedAlertContent(
                        category = currentCategory.value,
                        reason = currentReason.value,
                        sourceApp = currentSourceApp.value,
                        pattern = currentPattern.value,
                        onDismiss = { dismissAndFinish() },
                        onOpenAiCoach = { dismissAndOpenAiCoach() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updateFromIntent(intent)
        vibrateWarning()
    }

    private fun updateFromIntent(intent: Intent?) {
        if (intent == null) return
        currentCategory.value = intent.getStringExtra(EXTRA_CATEGORY) ?: "PMO_ALERT"
        currentReason.value = intent.getStringExtra(EXTRA_REASON) ?: "Akses konten dicegat oleh sistem Anti-PMO Shield"
        currentSourceApp.value = intent.getStringExtra(EXTRA_SOURCE_APP) ?: "Browser/App"
        currentPattern.value = intent.getStringExtra(EXTRA_PATTERN) ?: intent.getStringExtra(EXTRA_TRIGGER) ?: ""
    }

    override fun onResume() {
        super.onResume()
        AntiPmoAccessibilityService.isAlertCurrentlyShowing = true
    }

    override fun onPause() {
        super.onPause()
        AntiPmoAccessibilityService.isAlertCurrentlyShowing = false
    }

    private fun dismissAndFinish() {
        AntiPmoAccessibilityService.isAlertCurrentlyShowing = false
        AntiPmoAccessibilityService.lastDismissTimeMs = SystemClock.elapsedRealtime()

        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(homeIntent)
        } catch (_: Exception) {
        }

        finish()
    }

    private fun dismissAndOpenAiCoach() {
        AntiPmoAccessibilityService.isAlertCurrentlyShowing = false
        AntiPmoAccessibilityService.lastDismissTimeMs = SystemClock.elapsedRealtime()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TAB", "AI_COACH")
        }
        startActivity(intent)
        finish()
    }

    private fun vibrateWarning() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(
                        VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    v?.vibrate(300)
                }
            }
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AntiPmoAccessibilityService.isAlertCurrentlyShowing = false
        AntiPmoAccessibilityService.lastDismissTimeMs = SystemClock.elapsedRealtime()
    }
}

@Composable
fun BlockedAlertContent(
    category: String,
    reason: String,
    sourceApp: String,
    pattern: String,
    onDismiss: () -> Unit,
    onOpenAiCoach: () -> Unit
) {
    var countdownSeconds by remember { mutableIntStateOf(6) }
    var breathingPhase by remember { mutableStateOf("Tarik napas") }
    var breathingSubtext by remember { mutableStateOf("Tarik napas lewat hidung selama 4 detik") }
    val breathScale = remember { Animatable(1f) }

    val reminders = listOf(
        "Ini bukan keadaan darurat. Ini kebiasaan yang sedang minta perhatian.",
        "Anda sedang memilih. Pilih yang tidak perlu disesuaikan nanti.",
        "Dorongan lewat. Tidak perlu membuktikan apa apa.",
        "Satu klik lagi dan Anda menambah satu hari pemulihan.",
        "Berhenti sekarang. Tidak ada yang hilang jika Anda menunda."
    )
    val reminder = remember(category, pattern) { reminders.random() }

    LaunchedEffect(category, pattern) {
        countdownSeconds = 6
    }

    LaunchedEffect(Unit) {
        while (countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds--
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            breathingPhase = "Tarik napas"
            breathingSubtext = "Tarik napas lewat hidung selama 4 detik"
            breathScale.animateTo(1.12f, tween(4000, easing = FastOutSlowInEasing))
            breathingPhase = "Tahan napas"
            breathingSubtext = "Tahan selama 4 detik"
            delay(4000L)
            breathingPhase = "Hembuskan"
            breathingSubtext = "Hembuskan perlahan lewat mulut selama 4 detik"
            breathScale.animateTo(1f, tween(4000, easing = FastOutSlowInEasing))
            delay(1000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Akses dicegat",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kategori: $category",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (sourceApp.isNotBlank()) {
                    Text(
                        text = "Aplikasi: $sourceApp",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (pattern.isNotBlank()) {
                    Text(
                        text = "Pola terdeteksi: $pattern",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Jeda dan napas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .scale(breathScale.value)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = breathingPhase,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Text(
                    text = breathingSubtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = reminder,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onOpenAiCoach,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_talk_ai_coach"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bicara dengan AI Coach")
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_dismiss_alert"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (countdownSeconds > 0) {
                        "Saya sadar, tunggu $countdownSeconds detik"
                    } else {
                        "Saya sadar, kembali fokus"
                    }
                )
            }
        }
    }
}
