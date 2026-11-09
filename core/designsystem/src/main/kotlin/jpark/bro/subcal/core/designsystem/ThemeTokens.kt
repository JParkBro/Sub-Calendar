// 생성 파일 — 직접 고치지 않는다. 원본: docs/subcal/assets/theme-tokens.json
// 다시 만들기: python3 scripts/generate-theme-colors.py
@file:Suppress("MagicNumber")

package jpark.bro.subcal.core.designsystem

import androidx.compose.ui.graphics.Color

/** 인디고 · 라이트 */
internal val IndigoLight =
    AppColors(
        primary = Color(0xFF3D4FD6),
        primaryPressed = Color(0xFF3141B8),
        onPrimary = Color(0xFFFFFFFF),
        primarySoft = Color(0xFFECEEFC),
        onPrimarySoft = Color(0xFF2A3699),
        accent = Color(0xFF14A38F),
        accentSoft = Color(0xFFE2F5F1),
        onAccentSoft = Color(0xFF0B6457),
        bg = Color(0xFFF6F7F9),
        surface = Color(0xFFFFFFFF),
        surfaceRaised = Color(0xFFFFFFFF),
        fill = Color(0xFFEEF0F3),
        overlay = Color(0x730F121E),
        border = Color(0xFFE4E7EC),
        borderStrong = Color(0xFFC2C7D0),
        textPrimary = Color(0xFF161A23),
        textSecondary = Color(0xFF5A616E),
        textTertiary = Color(0xFF848A95),
        danger = Color(0xFFC62828),
        dangerSoft = Color(0xFFFDECEC),
        success = Color(0xFF127235),
        successSoft = Color(0xFFE6F4EA),
        warning = Color(0xFFB45309),
        warningSoft = Color(0xFFFEF3E2),
        calSunday = Color(0xFFC62828),
        calSaturday = Color(0xFF3156C4),
        shadow = Color(0x1F3D4FD6),
    )

/** 인디고 · 다크 */
internal val IndigoDark =
    AppColors(
        primary = Color(0xFF8E9BFF),
        primaryPressed = Color(0xFFA7B1FF),
        onPrimary = Color(0xFF111640),
        primarySoft = Color(0xFF242A58),
        onPrimarySoft = Color(0xFFD4D9FF),
        accent = Color(0xFF3FCDB8),
        accentSoft = Color(0xFF123A35),
        onAccentSoft = Color(0xFFA3EDE2),
        bg = Color(0xFF111318),
        surface = Color(0xFF1A1D24),
        surfaceRaised = Color(0xFF242833),
        fill = Color(0xFF2A2E38),
        overlay = Color(0xA6000000),
        border = Color(0xFF2C313C),
        borderStrong = Color(0xFF4A505D),
        textPrimary = Color(0xFFEDEFF3),
        textSecondary = Color(0xFFA9AFBB),
        textTertiary = Color(0xFF737A88),
        danger = Color(0xFFFF8A80),
        dangerSoft = Color(0xFF3B1F1E),
        success = Color(0xFF6DD58C),
        successSoft = Color(0xFF17331F),
        warning = Color(0xFFF5B35B),
        warningSoft = Color(0xFF3A2A14),
        calSunday = Color(0xFFFF8A80),
        calSaturday = Color(0xFF93ABFF),
        shadow = Color(0x66000000),
    )

internal fun palette(dark: Boolean): AppColors = if (dark) IndigoDark else IndigoLight

/** 테마와 무관한 고정 색 (theme-guide 5장) */
object AppFixedColors {
    private val serviceColors =
        mapOf(
            "RED" to Color(0xFFD93025),
            "ORANGE" to Color(0xFFC2570C),
            "AMBER" to Color(0xFFA86200),
            "GREEN" to Color(0xFF188038),
            "TEAL" to Color(0xFF00796B),
            "BLUE" to Color(0xFF1A73E8),
            "PURPLE" to Color(0xFF8430CE),
            "PINK" to Color(0xFFC2185B),
            "BROWN" to Color(0xFF795548),
            "BLACK" to Color(0xFF202124),
        )

    /** 서비스 아이콘 위 이니셜 */
    val onService = Color(0xFFFFFFFF)

    /** [colorKey]의 서비스 색. 모르는 키(옛 백업 등)는 BLACK으로 그린다 */
    fun service(colorKey: String): Color = serviceColors[colorKey] ?: serviceColors.getValue("BLACK")
}
