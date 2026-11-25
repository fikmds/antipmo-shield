package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AiCoachScreen
import com.example.ui.screens.BlockedLogsScreen
import com.example.ui.screens.EmergencyShieldScreen
import com.example.ui.screens.FilterRulesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialTab = intent.getStringExtra("OPEN_TAB")

        setContent {
            MyApplicationTheme {
                AntiPmoAppRoot(
                    viewModel = viewModel,
                    initialTab = initialTab
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions()
    }
}

private data class NavDestination(
    val label: String,
    val icon: ImageVector,
    val tag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AntiPmoAppRoot(
    viewModel: MainViewModel,
    initialTab: String? = null
) {
    val destinations = listOf(
        NavDestination("Beranda", Icons.Default.Security, "nav_tab_home"),
        NavDestination("Aturan", Icons.Default.Code, "nav_tab_filters"),
        NavDestination("Riwayat", Icons.Default.History, "nav_tab_logs"),
        NavDestination("AI", Icons.Default.Psychology, "nav_tab_ai_coach")
    )

    var selectedNavIndex by remember {
        mutableIntStateOf(if (initialTab == "AI_COACH") 3 else 0)
    }
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsStateWithLifecycle()

    LaunchedEffect(initialTab) {
        if (initialTab == "AI_COACH") {
            selectedNavIndex = 3
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .border(
                                1.dp,
                                if (isAccessibilityEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                                RoundedCornerShape(100.dp)
                            )
                    ) {
                        Text(
                            text = if (isAccessibilityEnabled) "Perlindungan aktif" else "Belum aktif",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAccessibilityEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedNavIndex == index,
                        onClick = { selectedNavIndex = index },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = when (destination.tag) {
                            "nav_tab_home" -> Modifier.testTag("nav_tab_home")
                            "nav_tab_filters" -> Modifier.testTag("nav_tab_filters")
                            "nav_tab_logs" -> Modifier.testTag("nav_tab_logs")
                            "nav_tab_ai_coach" -> Modifier.testTag("nav_tab_ai_coach")
                            else -> Modifier
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedNavIndex) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToEmergency = { selectedNavIndex = 4 },
                    onNavigateToLogs = { selectedNavIndex = 2 },
                    onNavigateToFilters = { selectedNavIndex = 1 }
                )
                1 -> FilterRulesScreen(viewModel = viewModel)
                2 -> BlockedLogsScreen(viewModel = viewModel)
                3 -> AiCoachScreen(viewModel = viewModel)
                4 -> EmergencyShieldScreen(viewModel = viewModel)
            }
        }
    }
}
