
package com.example.tradeflow.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalAppTheme = staticCompositionLocalOf { AppTheme() }

data class AppTheme(
    val isDarkMode: Boolean = false,
    val themeMode: String = "system"
)

private val TradeFlowLightColorScheme = lightColorScheme(
    primary = TradeFlowPrimary,           // Greenish = Color(0xFF007D70)
    onPrimary = White,
    primaryContainer = LightGreen,
    onPrimaryContainer = DarkGreen,

    secondary = TradeFlowSecondary,       // DarkGreen = Color(0xFF01423C)
    onSecondary = White,
    secondaryContainer = Green1,
    onSecondaryContainer = White,

    tertiary = TealBlue1,                 // Color(0xFF006B7D)
    onTertiary = White,
    tertiaryContainer = TealBlue,
    onTertiaryContainer = White,

    background = White,                   // Color(0xFFFCFCFC)
    onBackground = Black,

    surface = White,
    onSurface = Black,

    surfaceVariant = Color(0xFFE8F5E9),
    onSurfaceVariant = DarkGreen,

    outline = Greenish.copy(alpha = 0.5f),

    error = Reddish,                      // Color(0xFFF44336)
    onError = White,

    // Status bar colors
    surfaceTint = TradeFlowPrimary,

    // For cards and elevated surfaces
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFF5F5F5)
)

private val TradeFlowDarkColorScheme = darkColorScheme(
    primary = TradeFlowPrimary.copy(alpha = 0.9f),
    onPrimary = White,
    primaryContainer = DarkGreen,
    onPrimaryContainer = LightGreen,

    secondary = TradeFlowSecondary.copy(alpha = 0.8f),
    onSecondary = White,
    secondaryContainer = TealBlue,
    onSecondaryContainer = White,

    tertiary = TealBlue1.copy(alpha = 0.9f),
    onTertiary = White,
    tertiaryContainer = TealBlue,
    onTertiaryContainer = White,

    background = DarkBackground,          // Color(0xFF1E1E1E)
    onBackground = DarkText,              // Color(0xFFE0E0E0)

    surface = DarkSurface,                // Color(0xFF121212)
    onSurface = DarkText,

    surfaceVariant = DarkCard,            // Color(0xFF2D2D2D)
    onSurfaceVariant = DarkHint,          // Color(0xFF9E9E9E)

    outline = Greenish.copy(alpha = 0.3f),

    error = Reddish,
    onError = White,

    surfaceTint = TradeFlowPrimary,

    surfaceBright = Color(0xFF383838),
    surfaceDim = Color(0xFF121212)
)

@Composable
fun TradeFlowTheme(
    themeMode: String = "system",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        "system" -> darkTheme
        else -> darkTheme
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> TradeFlowDarkColorScheme
        else -> TradeFlowLightColorScheme
    }

    val appTheme = remember(themeMode, useDarkTheme) {
        AppTheme(isDarkMode = useDarkTheme, themeMode = themeMode)
    }

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun SetStatusBarColors(darkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
}