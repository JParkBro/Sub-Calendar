package jpark.bro.subcal.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider
import jpark.bro.subcal.core.designsystem.AppColors
import jpark.bro.subcal.core.designsystem.palette
import jpark.bro.subcal.core.model.WidgetBackground

/** 위젯 색 한 벌 (theme-guide 토큰을 그대로 쓴다) */
internal data class WidgetPalette(
    val background: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val danger: Color,
    val divider: Color,
)

/** 기기 라이트 · 다크 모드 각각의 색 */
internal data class WidgetColorScheme(
    val day: WidgetPalette,
    val night: WidgetPalette,
)

/** THEME은 기기 다크 모드를 따르고, DARK · WHITE는 고정 (product-spec 7장) */
internal fun widgetColorScheme(background: WidgetBackground): WidgetColorScheme {
    val light = palette(dark = false).toWidget()
    val dark = palette(dark = true).toWidget()
    return when (background) {
        WidgetBackground.THEME -> WidgetColorScheme(day = light, night = dark)
        WidgetBackground.DARK -> WidgetColorScheme(day = dark, night = dark)
        WidgetBackground.WHITE -> WidgetColorScheme(day = light, night = light)
    }
}

private fun AppColors.toWidget() = WidgetPalette(surface, textPrimary, textSecondary, danger, border)

/** Glance용 색 */
internal data class WidgetColors(
    val background: ColorProvider,
    val textPrimary: ColorProvider,
    val textSecondary: ColorProvider,
    val danger: ColorProvider,
    val divider: ColorProvider,
)

internal fun WidgetColorScheme.toGlance(): WidgetColors =
    WidgetColors(
        background = ColorProvider(day = day.background, night = night.background),
        textPrimary = ColorProvider(day = day.textPrimary, night = night.textPrimary),
        textSecondary = ColorProvider(day = day.textSecondary, night = night.textSecondary),
        danger = ColorProvider(day = day.danger, night = night.danger),
        divider = ColorProvider(day = day.divider, night = night.divider),
    )
