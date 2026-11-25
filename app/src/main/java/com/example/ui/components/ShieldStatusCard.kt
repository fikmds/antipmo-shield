package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun ShieldStatusCard(
    isAccessibilityEnabled: Boolean,
    isOverlayGranted: Boolean,
    blockedCount: Int,
    onTestPopup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var setupDismissed by remember { mutableStateOf(false) }

    val protectionLabel = if (isAccessibilityEnabled) "Perlindungan aktif" else "Perlindungan belum aktif"
    val protectionColor = if (isAccessibilityEnabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    val needsSetup = !isAccessibilityEnabled || (!isOverlayGranted && isAccessibilityEnabled)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Status perlindungan",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = protectionLabel,
                style = MaterialTheme.typography.titleSmall,
                color = protectionColor
            )

            Text(
                text = when {
                    isAccessibilityEnabled && isOverlayGranted ->
                        "Filter berjalan di latar belakang. Peringatan dapat tampil di atas aplikasi lain."
                    isAccessibilityEnabled ->
                        "Filter berjalan. Izin tampil di atas aplikasi lain belum diberikan, jadi peringatan mungkin tertutup aplikasi lain."
                    else ->
                        "Aktifkan layanan aksesibilitas sekali agar filter dapat memeriksa konten di aplikasi lain."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Aksesibilitas: ${if (isAccessibilityEnabled) "aktif" else "belum"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Overlay: ${if (isOverlayGranted) "aktif" else "belum"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Pencegatan: $blockedCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            if (needsSetup && !setupDismissed) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isAccessibilityEnabled) {
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_enable_accessibility")
                        ) {
                            Text("Aktifkan layanan aksesibilitas")
                        }
                    } else if (!isOverlayGranted) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                ).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_enable_overlay")
                        ) {
                            Text("Izinkan tampil di atas aplikasi lain")
                        }
                    }

                    TextButton(
                        onClick = { setupDismissed = true },
                        modifier = Modifier.testTag("btn_dismiss_permission_hint")
                    ) {
                        Text("Nanti saja", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            OutlinedButton(
                onClick = onTestPopup,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_test_popup_alert")
            ) {
                Text("Uji peringatan pencegatan")
            }
        }
    }
}
