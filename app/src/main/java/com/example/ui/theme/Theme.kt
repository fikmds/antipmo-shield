package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ShieldCyanDark,
    onPrimary = DarkBg,
    primaryContainer = DarkCard,
    onPrimaryContainer = DarkInk,
    secondary = ShieldCyanDark,
    onSecondary = DarkBg,
    secondaryContainer = DarkCard,
    onSecondaryContainer = DarkInk,
    error = ShieldWarningDark,
    onError = DarkBg,
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkMuted,
    outline = DarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ShieldCyan,
    onPrimary = LightBg,
    primaryContainer = LightCard,
    onPrimaryContainer = TextPrimary,
    secondary = ShieldCyan,
    onSecondary = LightBg,
    secondaryContainer = LightCard,
    onSecondaryContainer = TextPrimary,
    error = ShieldWarning,
    onError = LightBg,
    background = LightBg,
    onBackground = TextPrimary,
    surface = LightSurface,
    onSurface = TextPrimary,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextSecondary,
    outline = LightCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
