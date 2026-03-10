package com.ravenguard.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkAccent,
    background = DarkBackground,
    surface = DarkSurface
)

private val LightScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightAccent,
    background = LightBackground
)

@Composable
fun RavenGuardTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = Typography,
        content = content
    )
}
