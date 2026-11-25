package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.MainViewModel

@Composable
fun EmergencyShieldScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isEmergencyActive by viewModel.isEmergencyActive.collectAsStateWithLifecycle()
    val breathingPhase by viewModel.breathingPhase.collectAsStateWithLifecycle()
    val breathingSecondsLeft by viewModel.breathingSecondsLeft.collectAsStateWithLifecycle()
    val currentQuoteIndex by viewModel.currentQuoteIndex.collectAsStateWithLifecycle()

    var pushupCount by remember { mutableIntStateOf(0) }

    val breathScale by animateFloatAsState(
        targetValue = if (isEmergencyActive && breathingPhase.isExpanding()) 1.12f else 1f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "breathing_circle"
    )

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopEmergencyBreathing()
        }
    }

    val quote = viewModel.HARSH_MOTIVATIONAL_QUOTES.getOrElse(currentQuoteIndex) {
        viewModel.HARSH_MOTIVATIONAL_QUOTES.firstOrNull().orEmpty()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Mode darurat",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Berhenti sejenak. Ikuti napas atau lakukan pengalihan fisik sampai dorongan reda.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Pernapasan 4-7-8",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(breathScale)
                        .border(1.dp, MaterialTheme.colorScheme.error, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEmergencyActive) "$breathingSecondsLeft" else "4-7-8",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEmergencyActive) {
                                breathingPhase.toIndonesianPhase()
                            } else {
                                "Tekan mulai untuk berlatih"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isEmergencyActive) {
                            viewModel.stopEmergencyBreathing()
                        } else {
                            viewModel.startEmergencyBreathing()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEmergencyActive) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_toggle_breathing")
                ) {
                    Icon(
                        imageVector = if (isEmergencyActive) Icons.Default.Stop else Icons.Default.Air,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEmergencyActive) "Hentikan latihan" else "Mulai latihan")
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
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pengalihan fisik",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Push-up",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$pushupCount push-up tercatat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { pushupCount += 5 },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("btn_add_pushup")
                    ) {
                        Text("Tambah 5", style = MaterialTheme.typography.labelMedium)
                    }
                }

                QuickTip(
                    icon = Icons.Default.WaterDrop,
                    title = "Cuci muka dengan air dingin",
                    description = "Alihkan perhatian ke sensasi air dingin di wajah."
                )
                QuickTip(
                    icon = Icons.Default.LocalDrink,
                    title = "Minum segelas air",
                    description = "Berhenti sejenak sambil minum air putih."
                )
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
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pengingat keras",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = quote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = { viewModel.nextQuote() },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pengingat berikutnya")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun String.isExpanding(): Boolean =
    contains("Inhale", ignoreCase = true) || contains("Hold", ignoreCase = true)

private fun String.toIndonesianPhase(): String = when {
    contains("Inhale", ignoreCase = true) -> "Tarik napas 4 detik"
    contains("Hold", ignoreCase = true) -> "Tahan 4 detik"
    contains("Exhale", ignoreCase = true) -> "Hembuskan 4 detik"
    else -> this
}

@Composable
private fun QuickTip(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
