package com.example.ftprcar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Gold = Color(0xFFE8B923)
private val Blue = Color(0xFF0A2463)
private val BlueLight = Color(0xFF1E3A8A)
private val Teal = Color(0xFF00B4D8)
private val LightSurface = Color(0xFFF8FAFC)
private val DarkSurface = Color(0xFF0F172A)
private val LightCard = Color(0xFFFFFFFF)
private val DarkCard = Color(0xFF1E293B)

private val DarkColorScheme = darkColorScheme(
    primary = Teal,
    onPrimary = Color.Black,
    primaryContainer = BlueLight,
    onPrimaryContainer = Color.White,
    secondary = Gold,
    onSecondary = Color.Black,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkCard,
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = Teal.copy(alpha = 0.2f),
    onPrimaryContainer = Blue,
    secondary = Gold,
    onSecondary = Color.Black,
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightCard,
    onSurfaceVariant = Color(0xFF64748B)
)

@Composable
fun FTPRCarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
