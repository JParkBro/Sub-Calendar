package jpark.bro.subcal.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/** ThemeTokens.kt(생성 파일)가 원본 theme-tokens.json과 같은지 확인한다. */
class ThemeTokensSyncTest {
    private val json: JsonObject =
        Json.parseToJsonElement(File("../../docs/subcal/assets/theme-tokens.json").readText()).jsonObject

    @Test
    fun `인디고 2개 팔레트의 27개 토큰이 JSON과 같다`() {
        val indigo = json.getValue("indigo").jsonObject
        listOf(false, true).forEach { dark ->
            val mode = if (dark) "dark" else "light"
            val expected = indigo.getValue(mode).jsonObject
            val actual = palette(dark).byToken()

            assertEquals(27, expected.size)
            assertEquals(mode, expected.keys, actual.keys)
            expected.forEach { (token, value) ->
                assertEquals("$mode $token", parse(value.jsonPrimitive.content), actual.getValue(token).toArgb())
            }
        }
    }

    @Test
    fun `서비스 색 10종이 JSON과 같다`() {
        val service = json.getValue("service").jsonObject

        assertEquals(10, service.size)
        service.forEach { (key, value) ->
            assertEquals(key, parse(value.jsonPrimitive.content), AppFixedColors.service(key).toArgb())
        }
    }

    @Test
    fun `모르는 서비스 색은 BLACK으로 그린다`() {
        assertEquals(AppFixedColors.service("BLACK"), AppFixedColors.service("없는 색"))
    }

    /** #RRGGBB 또는 #RRGGBBAA → ARGB */
    private fun parse(value: String): Int {
        val digits = value.removePrefix("#")
        val rgb = digits.take(6).toLong(16)
        val alpha = if (digits.length == 8) digits.drop(6).toLong(16) else 0xFFL
        return ((alpha shl 24) or rgb).toInt()
    }

    private fun AppColors.byToken(): Map<String, Color> =
        mapOf(
            "primary" to primary,
            "primary-pressed" to primaryPressed,
            "on-primary" to onPrimary,
            "primary-soft" to primarySoft,
            "on-primary-soft" to onPrimarySoft,
            "accent" to accent,
            "accent-soft" to accentSoft,
            "on-accent-soft" to onAccentSoft,
            "bg" to bg,
            "surface" to surface,
            "surface-raised" to surfaceRaised,
            "fill" to fill,
            "overlay" to overlay,
            "border" to border,
            "border-strong" to borderStrong,
            "text-primary" to textPrimary,
            "text-secondary" to textSecondary,
            "text-tertiary" to textTertiary,
            "danger" to danger,
            "danger-soft" to dangerSoft,
            "success" to success,
            "success-soft" to successSoft,
            "warning" to warning,
            "warning-soft" to warningSoft,
            "cal-sunday" to calSunday,
            "cal-saturday" to calSaturday,
            "shadow" to shadow,
        )
}
