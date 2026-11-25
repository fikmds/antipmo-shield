package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomRule
import com.example.service.AntiPmoFilterEngine
import com.example.service.FilterMatch
import com.example.ui.BlockedAlertActivity
import com.example.ui.components.RegexTesterCard
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterRulesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customRules by viewModel.customRules.collectAsStateWithLifecycle()
    val filterAdult by viewModel.filterAdult.collectAsStateWithLifecycle()
    val filterTerabox by viewModel.filterTerabox.collectAsStateWithLifecycle()
    val filterUc by viewModel.filterUc.collectAsStateWithLifecycle()
    val browserOnlyMode by viewModel.browserOnlyMode.collectAsStateWithLifecycle()
    val whitelistedWords by viewModel.whitelistedWords.collectAsStateWithLifecycle()
    val whitelistedApps by viewModel.whitelistedApps.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var showAddWordDialog by remember { mutableStateOf(false) }
    var showAddAppDialog by remember { mutableStateOf(false) }
    var testInput by remember { mutableStateOf("") }

    val testResult: FilterMatch? = remember(
        testInput, customRules, filterAdult, filterTerabox, filterUc, whitelistedWords
    ) {
        if (testInput.isBlank()) {
            null
        } else {
            AntiPmoFilterEngine.evaluate(
                input = testInput,
                customRules = customRules,
                filterAdult = filterAdult,
                filterTerabox = filterTerabox,
                filterUc = filterUc,
                whitelistedWords = whitelistedWords
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Filter", style = MaterialTheme.typography.labelMedium) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Whitelist", style = MaterialTheme.typography.labelMedium) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Aturan", style = MaterialTheme.typography.labelMedium) }
            )
        }

        when (selectedTab) {
            0 -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Pengaturan filter",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                item {
                    ToggleRow(
                        title = "Mode khusus browser",
                        description = "Filter hanya memeriksa teks di aplikasi browser.",
                        checked = browserOnlyMode,
                        onCheckedChange = { viewModel.setBrowserOnlyMode(it) },
                        tagModifier = Modifier.testTag("switch_browser_only")
                    )
                }
                item {
                    ToggleRow(
                        title = "Filter konten dewasa",
                        description = "Mencegat kata kunci dan tautan kategori 18+.",
                        checked = filterAdult,
                        onCheckedChange = { viewModel.setFilterAdult(it) },
                        tagModifier = Modifier.testTag("switch_filter_adult")
                    )
                }
                item {
                    ToggleRow(
                        title = "Filter Terabox",
                        description = "Mencegat tautan layanan penyimpanan Terabox.",
                        checked = filterTerabox,
                        onCheckedChange = { viewModel.setFilterTerabox(it) },
                        tagModifier = Modifier.testTag("switch_filter_terabox")
                    )
                }
                item {
                    ToggleRow(
                        title = "Filter UC Browser",
                        description = "Mencegat pola tautan UC Browser.",
                        checked = filterUc,
                        onCheckedChange = { viewModel.setFilterUc(it) },
                        tagModifier = Modifier.testTag("switch_filter_uc")
                    )
                }
                item {
                    Text(
                        text = "Tester memakai pengaturan dan aturan yang sedang aktif.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sandbox_test")
                    ) {
                        RegexTesterCard(
                            testInput = testInput,
                            testResult = testResult,
                            onInputChange = { testInput = it },
                            onTriggerPopupSimulation = { input, match ->
                                launchSimulationAlert(context, input, match)
                            }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            1 -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Whitelist",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                item {
                    SectionHeader(
                        title = "Kata yang dikecualikan",
                        description = "Kata di daftar ini tidak akan dicegat.",
                        actionLabel = "Tambah kata",
                        onAction = { showAddWordDialog = true },
                        actionTagModifier = Modifier.testTag("btn_add_whitelist_word")
                    )
                }
                if (whitelistedWords.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada kata dikecualikan. Tambahkan kata agar tidak memicu filter saat mengetik.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(whitelistedWords.toList(), key = { it }) { word ->
                    RemovableRow(
                        label = word,
                        onRemove = { viewModel.removeWhitelistedWord(word) }
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
                item {
                    SectionHeader(
                        title = "Aplikasi yang dikecualikan",
                        description = "Package aplikasi di daftar ini tidak dipindai.",
                        actionLabel = "Tambah aplikasi",
                        onAction = { showAddAppDialog = true },
                        actionTagModifier = Modifier.testTag("btn_add_whitelist_app")
                    )
                }
                if (whitelistedApps.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada aplikasi dikecualikan. Tambahkan package name aplikasi kerja atau chat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(whitelistedApps.toList(), key = { it }) { app ->
                    RemovableRow(
                        label = app,
                        onRemove = { viewModel.removeWhitelistedApp(app) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            2 -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Aturan kustom",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                item {
                    SectionHeader(
                        title = "Aturan buatan sendiri",
                        description = "Tambahkan kata kunci atau pola regex yang ingin dicegat.",
                        actionLabel = "Tambah aturan",
                        onAction = { showAddRuleDialog = true },
                        actionTagModifier = Modifier.testTag("btn_add_custom_rule")
                    )
                }
                if (customRules.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada aturan kustom. Gunakan tombol tambah untuk membuat aturan pertama.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(customRules, key = { it.id }) { rule ->
                    RuleRow(
                        rule = rule,
                        onToggle = { enabled -> viewModel.toggleRule(rule, enabled) },
                        onDelete = { viewModel.deleteRule(rule) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    if (showAddRuleDialog) {
        var pattern by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var isRegex by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            title = { Text("Tambah aturan", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = pattern,
                        onValueChange = { pattern = it },
                        label = { Text("Kata kunci atau pola") },
                        supportingText = {
                            if (pattern.isBlank()) Text("Isi pola terlebih dahulu.")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Keterangan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gunakan regex",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = isRegex, onCheckedChange = { isRegex = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.insertCustomRule(pattern, "CUSTOM", isRegex, description)
                        showAddRuleDialog = false
                    },
                    enabled = pattern.isNotBlank()
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddRuleDialog = false }) { Text("Batal") }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showAddWordDialog) {
        var word by remember { mutableStateOf("") }
        SimpleInputDialog(
            title = "Tambah kata whitelist",
            label = "Kata atau frasa",
            value = word,
            onValueChange = { word = it },
            onDismiss = { showAddWordDialog = false },
            onConfirm = {
                viewModel.addWhitelistedWord(word.trim())
                showAddWordDialog = false
            }
        )
    }

    if (showAddAppDialog) {
        var app by remember { mutableStateOf("") }
        SimpleInputDialog(
            title = "Tambah aplikasi whitelist",
            label = "Package name",
            value = app,
            onValueChange = { app = it },
            onDismiss = { showAddAppDialog = false },
            onConfirm = {
                viewModel.addWhitelistedApp(app.trim())
                showAddAppDialog = false
            }
        )
    }
}

private fun launchSimulationAlert(context: Context, input: String, match: FilterMatch) {
    val intent = Intent(context, BlockedAlertActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(BlockedAlertActivity.EXTRA_CATEGORY, match.category)
        putExtra(BlockedAlertActivity.EXTRA_REASON, match.reason)
        putExtra(BlockedAlertActivity.EXTRA_SOURCE_APP, "Tester aturan filter")
        putExtra(BlockedAlertActivity.EXTRA_PATTERN, match.pattern.ifBlank { input })
    }
    context.startActivity(intent)
}

@Composable
private fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tagModifier: Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = tagModifier
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
    actionTagModifier: Modifier
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onAction,
            shape = RoundedCornerShape(8.dp),
            modifier = actionTagModifier
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(actionLabel, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun RemovableRow(label: String, onRemove: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus $label")
            }
        }
    }
}

@Composable
private fun RuleRow(
    rule: CustomRule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.pattern,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (rule.isRegex) "Regex" else "Kata kunci",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (rule.description.isNotBlank()) {
                    Text(
                        text = rule.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = rule.isEnabled, onCheckedChange = onToggle)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus aturan ${rule.pattern}")
            }
        }
    }
}

@Composable
private fun SimpleInputDialog(
    title: String,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                supportingText = {
                    if (value.isBlank()) Text("Isi kolom terlebih dahulu.")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = value.isNotBlank()) {
                Text("Tambahkan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
