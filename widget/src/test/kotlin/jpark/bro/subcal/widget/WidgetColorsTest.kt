package jpark.bro.subcal.widget

import jpark.bro.subcal.core.designsystem.palette
import jpark.bro.subcal.core.model.WidgetBackground
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetColorsTest {
    @Test
    fun `테마 배경은 기기 모드를 따르고 다크 · 화이트는 고정이다`() {
        val light = palette(dark = false).surface
        val dark = palette(dark = true).surface

        val theme = widgetColorScheme(WidgetBackground.THEME)
        assertEquals(light, theme.day.background)
        assertEquals(dark, theme.night.background)

        val fixedDark = widgetColorScheme(WidgetBackground.DARK)
        assertEquals(dark, fixedDark.day.background)
        assertEquals(dark, fixedDark.night.background)

        val white = widgetColorScheme(WidgetBackground.WHITE)
        assertEquals(light, white.day.background)
        assertEquals(light, white.night.background)
    }
}
