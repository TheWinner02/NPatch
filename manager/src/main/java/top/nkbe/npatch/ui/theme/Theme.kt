package top.nkbe.npatch.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import top.nkbe.npatch.config.Configs

private fun customColorScheme(colorName: String, isDark: Boolean): ColorScheme {
    return when (colorName) {
        "pixel_blue" -> {
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFFADC6FF),
                    onPrimary = Color(0xFF002E69),
                    primaryContainer = Color(0xFF004494),
                    onPrimaryContainer = Color(0xFFD8E2FF),
                    secondary = Color(0xFFC1C6DD),
                    onSecondary = Color(0xFF2B3042),
                    secondaryContainer = Color(0xFF414659),
                    onSecondaryContainer = Color(0xFFDDE1F9),
                    background = Color(0xFF1B1B1F),
                    surface = Color(0xFF1B1B1F),
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF1A73E8),
                    onPrimary = Color(0xFFFFFFFF),
                    primaryContainer = Color(0xFFD8E2FF),
                    onPrimaryContainer = Color(0xFF001B3D),
                    secondary = Color(0xFF595D72),
                    onSecondary = Color(0xFFFFFFFF),
                    secondaryContainer = Color(0xFFDDE1F9),
                    onSecondaryContainer = Color(0xFF161B2C),
                    background = Color(0xFFFEFDFD),
                    surface = Color(0xFFFEFDFD),
                )
            }
        }
        "mint_green" -> {
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFF6CDBAC),
                    onPrimary = Color(0xFF003823),
                    primaryContainer = Color(0xFF005235),
                    onPrimaryContainer = Color(0xFF89F8C7),
                    secondary = Color(0xFFB4CCBE),
                    onSecondary = Color(0xFF20352A),
                    secondaryContainer = Color(0xFF374C40),
                    onSecondaryContainer = Color(0xFFD0E8D9),
                    background = Color(0xFF191C1A),
                    surface = Color(0xFF191C1A),
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF006C48),
                    onPrimary = Color(0xFFFFFFFF),
                    primaryContainer = Color(0xFF89F8C7),
                    onPrimaryContainer = Color(0xFF002113),
                    secondary = Color(0xFF4E6354),
                    onSecondary = Color(0xFFFFFFFF),
                    secondaryContainer = Color(0xFFD0E8D9),
                    onSecondaryContainer = Color(0xFF0C1F13),
                    background = Color(0xFFFBFDF9),
                    surface = Color(0xFFFBFDF9),
                )
            }
        }
        "lavender" -> {
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFFC7BFFF),
                    onPrimary = Color(0xFF2E17A8),
                    primaryContainer = Color(0xFF4633C1),
                    onPrimaryContainer = Color(0xFFE5DEFF),
                    secondary = Color(0xFFC4C4DD),
                    onSecondary = Color(0xFF2D2F42),
                    secondaryContainer = Color(0xFF43455A),
                    onSecondaryContainer = Color(0xFFE0E1F9),
                    background = Color(0xFF1C1B1F),
                    surface = Color(0xFF1C1B1F),
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF5D4BB6),
                    onPrimary = Color(0xFFFFFFFF),
                    primaryContainer = Color(0xFFE5DEFF),
                    onPrimaryContainer = Color(0xFF190065),
                    secondary = Color(0xFF5C5D72),
                    onSecondary = Color(0xFFFFFFFF),
                    secondaryContainer = Color(0xFFE0E1F9),
                    onSecondaryContainer = Color(0xFF191A2C),
                    background = Color(0xFFFFFBFD),
                    surface = Color(0xFFFFFBFD),
                )
            }
        }
        "peach" -> {
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFFFFB4AB),
                    onPrimary = Color(0xFF690005),
                    primaryContainer = Color(0xFF93000A),
                    onPrimaryContainer = Color(0xFFFFDAD6),
                    secondary = Color(0xFFE7BDB8),
                    onSecondary = Color(0xFF442A27),
                    secondaryContainer = Color(0xFF5D403C),
                    onSecondaryContainer = Color(0xFFFFDAD6),
                    background = Color(0xFF201A19),
                    surface = Color(0xFF201A19),
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFFBA1A1A),
                    onPrimary = Color(0xFFFFFFFF),
                    primaryContainer = Color(0xFFFFDAD6),
                    onPrimaryContainer = Color(0xFF410002),
                    secondary = Color(0xFF775652),
                    onSecondary = Color(0xFFFFFFFF),
                    secondaryContainer = Color(0xFFFFDAD6),
                    onSecondaryContainer = Color(0xFF2C1512),
                    background = Color(0xFFFFF8F7),
                    surface = Color(0xFFFFF8F7),
                )
            }
        }
        "coral" -> {
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFFFFB59B),
                    onPrimary = Color(0xFF591D08),
                    primaryContainer = Color(0xFF76331C),
                    onPrimaryContainer = Color(0xFFFFDBCF),
                    secondary = Color(0xFFE7BDB1),
                    onSecondary = Color(0xFF442A22),
                    secondaryContainer = Color(0xFF5D4037),
                    onSecondaryContainer = Color(0xFFFFDBCF),
                    background = Color(0xFF201A18),
                    surface = Color(0xFF201A18),
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF93452A),
                    onPrimary = Color(0xFFFFFFFF),
                    primaryContainer = Color(0xFFFFDBCF),
                    onPrimaryContainer = Color(0xFF380D00),
                    secondary = Color(0xFF77574E),
                    onSecondary = Color(0xFFFFFFFF),
                    secondaryContainer = Color(0xFFFFDBCF),
                    onSecondaryContainer = Color(0xFF2C1610),
                    background = Color(0xFFFFF8F6),
                    surface = Color(0xFFFFF8F6),
                )
            }
        }
        else -> {
            if (isDark) darkColorScheme() else lightColorScheme()
        }
    }
}

@Composable
fun LSPTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    enableDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        enableDynamicColor && Configs.customAccentColor == "default" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        Configs.customAccentColor != "default" -> {
            customColorScheme(Configs.customAccentColor, isDarkTheme)
        }
        isDarkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.background.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

