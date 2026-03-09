package com.example.expensetracker.ui.theme

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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Extended semantic colors not covered by Material's colorScheme ──────────
data class ExtendedColors(
    val textSecondary: Color,
    val textTertiary: Color,
    val surfaceVariant: Color,
    val successBg: Color,
    val successText: Color,
    val warningBg: Color,
    val warningText: Color,
    val errorBg: Color,
    val errorText: Color,
    val infoBg: Color,
    val infoText: Color,
    val brandPrimary: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        textSecondary = TextSecondaryLight,
        textTertiary = TextTertiaryLight,
        surfaceVariant = SurfaceVariantLight,
        successBg = SuccessBg,
        successText = SuccessText,
        warningBg = WarningBg,
        warningText = WarningText,
        errorBg = ErrorBg,
        errorText = ErrorColor,
        infoBg = ActionBlueBg,
        infoText = ActionBlueText,
        brandPrimary = Primary
    )
}

private val LightExtended = ExtendedColors(
    textSecondary = TextSecondaryLight,
    textTertiary = TextTertiaryLight,
    surfaceVariant = SurfaceVariantLight,
    successBg = SuccessBg,
    successText = SuccessText,
    warningBg = WarningBg,
    warningText = WarningText,
    errorBg = ErrorBg,
    errorText = ErrorColor,
    infoBg = ActionBlueBg,
    infoText = ActionBlueText,
    brandPrimary = Primary
)

private val DarkExtended = ExtendedColors(
    textSecondary = TextSecondaryDark,
    textTertiary = TextTertiaryDark,
    surfaceVariant = SurfaceVariantDark,
    successBg = SuccessBgDark,
    successText = SuccessTextDark,
    warningBg = WarningBgDark,
    warningText = WarningTextDark,
    errorBg = ErrorBgDark,
    errorText = ErrorColorDark,
    infoBg = ActionBlueBgDark,
    infoText = ActionBlueTextDark,
    brandPrimary = PrimaryDark
)

// ─── Material 3 Schemes ─────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onPrimary = Color(0xFF121212),
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorColorDark
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onPrimary = Color.White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorColor
)

@Composable
fun ExpenseTrackerTheme(
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

    val extendedColors = if (darkTheme) DarkExtended else LightExtended

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Convenience accessor
object AppTheme {
    val extended: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}