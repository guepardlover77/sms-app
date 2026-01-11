package com.music.sms.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val LightColorScheme = lightColorScheme(
    primary = PastelPeriwinkle,
    onPrimary = TextPrimaryLight,
    primaryContainer = PastelLavender,
    onPrimaryContainer = TextPrimaryLight,
    secondary = PastelLilac,
    onSecondary = TextPrimaryLight,
    secondaryContainer = PastelVioletSoft,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = BubbleOutStart,
    onTertiary = TextOnBubbleOut,
    tertiaryContainer = BubbleOutEnd,
    onTertiaryContainer = TextOnBubbleOut,
    background = BackgroundPrimaryLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = BackgroundSecondaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = SeparatorLight,
    outlineVariant = SeparatorLight,
    error = ErrorRed,
    onError = TextOnBubbleOut
)

private val DarkColorScheme = darkColorScheme(
    primary = PastelPeriwinkleDark,
    onPrimary = TextPrimaryDark,
    primaryContainer = PastelLavenderDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = PastelLavenderDark,
    onSecondary = TextPrimaryDark,
    secondaryContainer = PastelPeriwinkleDark,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = BubbleOutStartDark,
    onTertiary = TextOnBubbleOut,
    tertiaryContainer = BubbleOutEndDark,
    onTertiaryContainer = TextOnBubbleOut,
    background = BackgroundPrimaryDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = BackgroundSecondaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SeparatorDark,
    outlineVariant = SeparatorDark,
    error = ErrorRed,
    onError = TextOnBubbleOut
)

@Composable
fun PastelSmsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val activity = view.context as? Activity
                activity?.window?.let { window ->
                    window.statusBarColor = colorScheme.background.toArgb()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        @Suppress("DEPRECATION")
                        window.decorView.systemUiVisibility = if (darkTheme) {
                            0
                        } else {
                            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore status bar styling errors
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
