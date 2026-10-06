package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SnapFindTheme(
    themeMode: String = "SYSTEM",
    colorTheme: String = "VIOLET_BLOOM",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val palette = colorPalettes.find { it.key == colorTheme } ?: colorPalettes[0]

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.primaryDark,
            onPrimary = Color.White,
            primaryContainer = palette.containerDark,
            onPrimaryContainer = palette.onContainerDark,
            secondary = palette.secondaryDark,
            onSecondary = Color.White,
            secondaryContainer = palette.containerDark,
            onSecondaryContainer = palette.onContainerDark,
            background = palette.backgroundDark,
            onBackground = palette.textDark,
            surface = palette.surfaceDark,
            onSurface = palette.textDark,
            surfaceVariant = palette.surfaceVariantDark,
            onSurfaceVariant = palette.secondaryTextDark,
            outline = palette.outlineDark,
            error = Color(0xFFEF4444),
            onError = Color.White,
            errorContainer = Color(0xFF7F1D1D),
            onErrorContainer = Color(0xFFFEE2E2)
        )
    } else {
        lightColorScheme(
            primary = palette.primaryLight,
            onPrimary = Color.White,
            primaryContainer = palette.containerLight,
            onPrimaryContainer = palette.onContainerLight,
            secondary = palette.secondaryLight,
            onSecondary = Color.White,
            secondaryContainer = palette.containerLight,
            onSecondaryContainer = palette.onContainerLight,
            background = palette.backgroundLight,
            onBackground = palette.textLight,
            surface = palette.surfaceLight,
            onSurface = palette.textLight,
            surfaceVariant = palette.surfaceVariantLight,
            onSurfaceVariant = palette.secondaryTextLight,
            outline = palette.outlineLight,
            error = Color(0xFFDC2626),
            onError = Color.White,
            errorContainer = Color(0xFFFEE2E2),
            onErrorContainer = Color(0xFF991B1B)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
