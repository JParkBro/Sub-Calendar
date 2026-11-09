package jpark.bro.subcal.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 테마 색 토큰 27개 (theme-guide.md 2장). 모든 팔레트가 같은 토큰을 가진다.
 * 컴포넌트는 AppThemeTokens.colors.* 로만 참조한다.
 */
@Immutable
data class AppColors(
    val primary: Color,
    val primaryPressed: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    val onPrimarySoft: Color,
    val accent: Color,
    val accentSoft: Color,
    val onAccentSoft: Color,
    val bg: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val fill: Color,
    val overlay: Color,
    val border: Color,
    val borderStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val danger: Color,
    val dangerSoft: Color,
    val success: Color,
    val successSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val calSunday: Color,
    val calSaturday: Color,
    val shadow: Color,
)
