package com.arfurnish.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrownDarkPrimary,
    secondary = BrownDarkSecondary,
    tertiary = BrownDarkTertiary,
    background = Color(0xFF1E1C1A),
    surface = Color(0xFF2D2A26),
    surfaceVariant = Color(0xFF3E3A36),
    onPrimary = Color(0xFF3E2723),
    primaryContainer = BrownDarkPrimary,
    onPrimaryContainer = Color(0xFF3E2723)
)

private val LightColorScheme = lightColorScheme(
    primary = BrownPrimary,
    secondary = BrownSecondary,
    tertiary = BrownTertiary,
    background = WarmBackground,
    surface = WarmSurface,
    surfaceVariant = WarmSurfaceVariant,
    onPrimary = Color.White,
    primaryContainer = BrownPrimary,
    onPrimaryContainer = Color.White,
    onBackground = Color(0xFF2C1E16),
    onSurface = Color(0xFF2C1E16)
)

@Composable
fun ARFurnishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false by default so our custom brown theme isn't overwritten by Android 12+ wallpaper colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
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
