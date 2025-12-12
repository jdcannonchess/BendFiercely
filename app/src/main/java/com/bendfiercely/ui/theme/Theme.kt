package com.bendfiercely.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.White,
    primaryContainer = AmberPrimaryLight,
    onPrimaryContainer = TextPrimary,
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = CoralLight,
    onSecondaryContainer = TextPrimary,
    tertiary = Success,
    onTertiary = Color.White,
    tertiaryContainer = SuccessLight,
    onTertiaryContainer = TextPrimary,
    background = Cream,
    onBackground = TextPrimary,
    surface = WarmWhite,
    onSurface = TextPrimary,
    surfaceVariant = CreamDark,
    onSurfaceVariant = TextSecondary,
    outline = WarmGray,
    outlineVariant = CreamDark
)

private val DarkColorScheme = darkColorScheme(
    primary = AmberPrimaryLight,
    onPrimary = TextPrimary,
    primaryContainer = AmberPrimaryDark,
    onPrimaryContainer = Cream,
    secondary = CoralLight,
    onSecondary = TextPrimary,
    secondaryContainer = CoralDark,
    onSecondaryContainer = Cream,
    tertiary = SuccessLight,
    onTertiary = TextPrimary,
    tertiaryContainer = Success,
    onTertiaryContainer = Cream,
    background = DarkBackground,
    onBackground = Cream,
    surface = DarkSurface,
    onSurface = Cream,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = CreamDark,
    outline = TextSecondary,
    outlineVariant = DarkSurfaceVariant
)

@Composable
fun BendFiercelyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

