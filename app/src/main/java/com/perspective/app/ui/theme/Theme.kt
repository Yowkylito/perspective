package com.perspective.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Ivory,
    background = Ivory,
    onBackground = Ink,
    surface = Ivory,
    onSurface = Ink,
    surfaceVariant = Ivory08,
    onSurfaceVariant = Ivory55,
    outline = Ivory25,
    outlineVariant = Ivory08,
)

private val DarkScheme = darkColorScheme(
    primary = Paper,
    onPrimary = Ink,
    background = Ink,
    onBackground = Paper,
    surface = Ink,
    onSurface = Paper,
    surfaceVariant = Night10,
    onSurfaceVariant = Night62,
    outline = Night35,
    outlineVariant = Night10,
)

/**
 * Dynamic colour is intentionally not wired up. The brand is the palette; letting
 * the device wallpaper recolour it would undo the point of a monochrome system.
 */
@Composable
fun PerspectiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        shapes = PerspectiveShapes,
        typography = PerspectiveTypography,
        content = content,
    )
}
