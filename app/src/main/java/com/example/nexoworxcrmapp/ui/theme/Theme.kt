package com.example.nexoworxcrmapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NexoworxColorScheme = lightColorScheme(
    primary = Forest,
    onPrimary = CrmSurface,
    secondary = AccentGreen,
    onSecondary = Charcoal,
    background = CrmBg,
    onBackground = Charcoal,
    surface = CrmSurface,
    onSurface = Charcoal,
)

@Composable
fun NexoworxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NexoworxColorScheme,
        content = content,
    )
}
