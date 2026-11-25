package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BlockedLog
import com.example.ui.BlockedAlertActivity
import com.example.ui.components.ShieldStatusCard
import com.example.ui.components.StreakRecoveryCard
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToEmergency: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsStateWithLifecycle()
    val isOverlayGranted by viewModel.isOverlayGranted.collectAsStateWithLifecycle()
    val blockedCount by viewModel.blockedCount.collectAsStateWithLifecycle()
    val streakInfo by viewModel.streakInfo.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val currentQuoteIndex by viewModel.currentQuoteIndex.collectAsStateWithLifecycle()

    val recentLogs = allLogs.take(3)
    val quote = viewModel.HARSH_MOTIVATIONAL_QUOTES.getOrElse(currentQuoteIndex) {
        viewModel.HARSH_MOTIVATIONAL_QUOTES.firstOrNull().orEmpty()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Kendali harian",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        item {
            ShieldStatusCard(
                isAccessibilityEnabled = isAccessibilityEnabled,
                isOverlayGranted = isOverlayGranted,
                blockedCount = blockedCount,
                onTestPopup = { launchTestAlert(context) }
            )
        }

        item {
            StreakRecoveryCard(
                streakInfo = streakInfo,
                onResetStreak = { viewModel.resetStreak() }
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .testTag("nav_tab_emergency")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sedang ada dorongan?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Buka mode darurat untuk napas terkontrol dan pengalihan fisik.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onNavigateToEmergency,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("btn_open_emergency")
                    ) {
                        Text("Darurat", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item {
            TextButton(onClick = onNavigateToFilters) {
                Text("Atur filter dan pengecualian", style = MaterialTheme.typography.bodyMedium)
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pengingat keras",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.nextQuote() }) {
                            Icon(
                                imageVector = Icons.Default.NavigateNext,
                                contentDescription = "Pengingat berikutnya"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = quote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pencegatan terbaru",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = onNavigateToLogs,
                            modifier = Modifier.testTag("btn_see_all_logs")
                        ) {
                            Text("Lihat semua", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (recentLogs.isEmpty()) {
                        Text(
                            text = "Belum ada pencegatan tercatat. Setiap blokir akan muncul di sini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recentLogs.forEach { log -> BlockedMiniRow(log = log) }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun launchTestAlert(context: Context) {
    val intent = Intent(context, BlockedAlertActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(BlockedAlertActivity.EXTRA_TRIGGER, "Uji coba filter")
        putExtra(BlockedAlertActivity.EXTRA_CATEGORY, "TEST_SIMULASI")
        putExtra(BlockedAlertActivity.EXTRA_REASON, "Simulasi pencegatan untuk menguji tampilan peringatan")
        putExtra(BlockedAlertActivity.EXTRA_SOURCE_APP, "Anti PMO Shield")
    }
    context.startActivity(intent)
}

@Composable
private fun BlockedMiniRow(log: BlockedLog) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(log.timestamp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.matchedPattern.ifBlank { log.targetUrlOrText },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${log.category}, $dateString",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
