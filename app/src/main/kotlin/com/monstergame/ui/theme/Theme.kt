package com.monstergame.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = AccentBlue,
    onPrimary        = Color.Black,
    primaryContainer = DarkCard,
    secondary        = AccentGold,
    onSecondary      = Color.Black,
    background       = DarkBackground,
    onBackground     = TextPrimary,
    surface          = DarkSurface,
    onSurface        = TextPrimary,
    surfaceVariant   = DarkCard,
    onSurfaceVariant = TextSecondary,
    error            = AccentRed,
    onError          = Color.White
)

@Composable
fun MonsterRPGTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = MonsterTypography,
        content     = content
    )
}
