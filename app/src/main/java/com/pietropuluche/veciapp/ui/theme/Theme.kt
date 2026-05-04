package com.pietropuluche.veciapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = SkyBlue,
    onPrimary = SurfaceCard,
    secondary = DeepOcean,
    tertiary = Mint,
    background = WarmBackground,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    onBackground = TextPrimary,
    error = AlertRed
)

private val DarkColors = darkColorScheme(
    primary = SkyBlue,
    secondary = Mint,
    background = DeepOcean,
    surface = ColorTokens.darkSurface,
    onSurface = SurfaceCard,
    onBackground = SurfaceCard,
    error = AlertRed
)

private object ColorTokens {
    val darkSurface = androidx.compose.ui.graphics.Color(0xFF12395E)
}

@Composable
fun VeciAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
