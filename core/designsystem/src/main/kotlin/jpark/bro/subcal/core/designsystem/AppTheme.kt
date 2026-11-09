package jpark.bro.subcal.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import jpark.bro.subcal.core.model.AppThemeMode

val LocalAppColors = staticCompositionLocalOf<AppColors> { error("AppTheme 밖에서 AppThemeTokens를 사용했습니다.") }
val LocalAppTypography = staticCompositionLocalOf<AppTypography> { error("AppTheme 밖에서 AppThemeTokens를 사용했습니다.") }

/**
 * 앱 테마 (theme-guide.md 8장). 화면 모드는 DataStore에 저장하고 위젯도 같은 팔레트를 쓴다.
 * Material 컴포넌트도 토큰 색 · 글자 스타일을 쓰도록 colorScheme · typography를 매핑한다.
 */
@Composable
fun AppTheme(
    mode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = isDarkTheme(mode)
    val colors = palette(dark)
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides DefaultTypography,
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(dark),
            typography = DefaultTypography.toMaterialTypography(),
            content = content,
        )
    }
}

/** 화면 모드를 실제 다크 여부로 바꾼다. SYSTEM은 기기 설정을 따른다. */
@Composable
fun isDarkTheme(mode: AppThemeMode): Boolean =
    when (mode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

object AppThemeTokens {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current
}

private fun AppColors.toColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primarySoft,
        onPrimaryContainer = onPrimarySoft,
        secondary = accent,
        secondaryContainer = accentSoft,
        onSecondaryContainer = onAccentSoft,
        background = bg,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = fill,
        onSurfaceVariant = textSecondary,
        surfaceContainer = surface,
        surfaceContainerHigh = surfaceRaised,
        surfaceContainerHighest = surfaceRaised,
        outline = borderStrong,
        outlineVariant = border,
        error = danger,
        errorContainer = dangerSoft,
        onErrorContainer = danger,
        scrim = overlay,
    )
}
