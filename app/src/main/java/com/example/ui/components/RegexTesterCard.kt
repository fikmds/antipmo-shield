package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.service.FilterMatch

@Composable
fun RegexTesterCard(
    testInput: String,
    testResult: FilterMatch?,
    onInputChange: (String) -> Unit,
    onTriggerPopupSimulation: (String, FilterMatch) -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = "Uji filter",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Ketik teks atau tautan untuk melihat apakah filter akan mencegatnya.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = testInput,
                onValueChange = onInputChange,
                label = { Text("Teks atau tautan") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_regex_test"),
                shape = RoundedCornerShape(8.dp),
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onInputChange("https://terabox.com/s/1contoh") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Terabox", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { onInputChange("download via uc browser link") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("UC Browser", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { onInputChange("video bokep indo") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("18+", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (testInput.isBlank()) {
                Text(
                    text = "Masukkan teks untuk melihat hasil uji.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (testResult != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (testResult.matched) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = if (testResult.matched) "Dicegat" else "Lolos",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (testResult.matched) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = testResult.reason.ifBlank {
                                if (testResult.matched) {
                                    "Teks cocok dengan aturan ${testResult.category}."
                                } else {
                                    "Tidak ada aturan yang cocok."
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (testResult.matched) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onTriggerPopupSimulation(testInput, testResult) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("btn_simulate_intercept_from_tester")
                            ) {
                                Text("Tampilkan peringatan")
                            }
                        }
                    }
                }
            }
        }
    }
}
