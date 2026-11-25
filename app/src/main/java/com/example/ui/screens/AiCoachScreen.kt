package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatMessage
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val aiBaseUrl by viewModel.aiBaseUrl.collectAsStateWithLifecycle()
    val aiApiKey by viewModel.aiApiKey.collectAsStateWithLifecycle()
    val aiModel by viewModel.aiModel.collectAsStateWithLifecycle()
    val aiSystemPrompt by viewModel.aiSystemPrompt.collectAsStateWithLifecycle()
    val availableModels by viewModel.availableModels.collectAsStateWithLifecycle()
    val isFetchingModels by viewModel.isFetchingModels.collectAsStateWithLifecycle()
    val fetchModelsError by viewModel.fetchModelsError.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiSending by viewModel.isAiSending.collectAsStateWithLifecycle()
    val chatError by viewModel.chatError.collectAsStateWithLifecycle()

    var showConfigDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size, isAiSending) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "Saya merasakan dorongan kuat sekarang, bantu saya sadar kembali",
        "Jelaskan dampak dopamin murah pada motivasi",
        "Berikan strategi untuk tujuh hari pertama",
        "Beri saya pengingat keras sekarang",
        "Pandu grounding mental selama dua menit"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Recovery Coach",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (aiApiKey.isBlank()) {
                            "API key belum diisi"
                        } else {
                            "Model: ${aiModel.ifBlank { "belum dipilih" }}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Bersihkan percakapan"
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = { showConfigDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_open_ai_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pengaturan", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (aiApiKey.isBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(0.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Isi base URL, API key, dan model untuk mulai chat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { showConfigDialog = true }) {
                        Text("Atur", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (chatMessages.isEmpty() && !isAiSending) {
                item {
                    Text(
                        text = "Belum ada percakapan. Kirim pesan untuk mulai bicara dengan coach.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            items(chatMessages, key = { it.id }) { message ->
                ChatMessageBubble(message = message)
            }
            if (isAiSending) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Coach sedang menyusun jawaban",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (chatError != null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.error,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = chatError ?: "Pesan gagal dikirim",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(onClick = { showConfigDialog = true }) {
                            Text("Periksa pengaturan", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                OutlinedButton(
                    onClick = { viewModel.sendUserChatMessage(prompt) },
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isAiSending
                ) {
                    Text(prompt, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Surface(color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Tulis pesan") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_ai_chat"),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.sendUserChatMessage(inputText)
                        inputText = ""
                    },
                    enabled = inputText.isNotBlank() && !isAiSending,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_send_ai_chat")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Kirim pesan"
                    )
                }
            }
        }
    }

    if (showConfigDialog) {
        AiConfigDialog(
            initialBaseUrl = aiBaseUrl,
            initialApiKey = aiApiKey,
            initialModel = aiModel,
            initialSystemPrompt = aiSystemPrompt,
            availableModels = availableModels,
            isFetchingModels = isFetchingModels,
            fetchModelsError = fetchModelsError,
            onFetchModels = { url, key -> viewModel.fetchModelsFromApi(url, key) },
            onDismiss = { showConfigDialog = false },
            onSave = { url, key, model, prompt ->
                viewModel.saveAiConfig(url, key, model, prompt)
                showConfigDialog = false
            }
        )
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isUser) "Anda" else "Coach",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConfigDialog(
    initialBaseUrl: String,
    initialApiKey: String,
    initialModel: String,
    initialSystemPrompt: String,
    availableModels: List<String>,
    isFetchingModels: Boolean,
    fetchModelsError: String?,
    onFetchModels: (baseUrl: String, apiKey: String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (baseUrl: String, apiKey: String, model: String, systemPrompt: String) -> Unit
) {
    var baseUrl by remember { mutableStateOf(initialBaseUrl) }
    var apiKey by remember { mutableStateOf(initialApiKey) }
    var model by remember { mutableStateOf(initialModel) }
    var systemPrompt by remember { mutableStateOf(initialSystemPrompt) }
    var showApiKey by remember { mutableStateOf(false) }
    var showModelMenu by remember { mutableStateOf(false) }

    val presetProviders = listOf(
        "OpenAI" to "https://api.openai.com/v1",
        "OpenRouter" to "https://openrouter.ai/api/v1",
        "Groq" to "https://api.groq.com/openai/v1",
        "DeepSeek" to "https://api.deepseek.com/v1",
        "Ollama lokal" to "http://10.0.2.2:11434/v1"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pengaturan AI",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Preset server",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presetProviders) { (name, url) ->
                            OutlinedButton(
                                onClick = { baseUrl = url },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(name, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("Base URL") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_ai_base_url"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API key") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_ai_api_key"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        visualTransformation = if (showApiKey) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    imageVector = if (showApiKey) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = if (showApiKey) {
                                        "Sembunyikan API key"
                                    } else {
                                        "Tampilkan API key"
                                    }
                                )
                            }
                        }
                    )
                }
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = model,
                            onValueChange = { model = it },
                            label = { Text("Model") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_ai_model"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = { showModelMenu = true },
                                    enabled = availableModels.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = "Pilih model"
                                    )
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = showModelMenu,
                            onDismissRequest = { showModelMenu = false }
                        ) {
                            availableModels.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        model = option
                                        showModelMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onFetchModels(baseUrl, apiKey) },
                            enabled = !isFetchingModels && baseUrl.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_fetch_models")
                        ) {
                            if (isFetchingModels) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isFetchingModels) "Memuat" else "Ambil daftar model",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        when {
                            fetchModelsError != null -> Text(
                                text = "Gagal memuat model: ${fetchModelsError}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            availableModels.isEmpty() -> Text(
                                text = "Belum ada daftar model. Isi base URL lalu ambil daftar model.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            else -> Text(
                                text = "${availableModels.size} model tersedia dari server",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = systemPrompt,
                        onValueChange = { systemPrompt = it },
                        label = { Text("Instruksi sistem") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("input_ai_system_prompt"),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 4
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(baseUrl, apiKey, model, systemPrompt) },
                modifier = Modifier.testTag("btn_save_ai_config")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
