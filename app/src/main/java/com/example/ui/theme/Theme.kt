package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = CleanMinPrimary,
    onPrimary = CleanMinOnPrimary,
    primaryContainer = CleanMinPrimaryContainer,
    onPrimaryContainer = CleanMinOnPrimaryContainer,
    secondary = CleanMinSecondary,
    onSecondary = CleanMinOnSecondary,
    secondaryContainer = CleanMinSecondaryContainer,
    onSecondaryContainer = CleanMinOnSecondaryContainer,
    tertiary = CleanMinPrimary,
    background = CleanMinBackground,
    onBackground = CleanMinOnBackground,
    surface = CleanMinSurface,
    onSurface = CleanMinOnSurface,
    surfaceVariant = CleanMinSurfaceVariant,
    onSurfaceVariant = CleanMinOnSurfaceVariant,
    outline = CleanMinOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = CleanMinPrimaryContainer,
    onPrimary = CleanMinOnPrimaryContainer,
    primaryContainer = CleanMinPrimaryContainerDark,
    onPrimaryContainer = CleanMinOnPrimaryContainerDark,
    secondary = CleanMinSecondaryContainer,
    onSecondary = CleanMinOnSecondaryContainer,
    secondaryContainer = CleanMinSecondary,
    onSecondaryContainer = CleanMinOnSecondary,
    background = CleanMinBackgroundDark,
    onBackground = CleanMinOnBackgroundDark,
    surface = CleanMinSurfaceDark,
    onSurface = CleanMinOnSurfaceDark,
    surfaceVariant = CleanMinSurfaceVariantDark,
    onSurfaceVariant = CleanMinOnSurfaceVariantDark,
    outline = CleanMinOutlineDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
