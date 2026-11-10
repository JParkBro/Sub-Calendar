package jpark.bro.subcal.widget

import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.testing.SampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** product-spec 9장 샘플 (오늘 = 2026.10.30). 홈과 같은 값이어야 한다 (widget/CLAUDE.md) */
class WidgetDisplayTest {
    private val today = SampleData.today

    @Test
    fun `이번 달 구독료와 다가오는 결제 2건은 홈과 같다`() {
        val display = buildWidgetDisplay(SampleData.records, AppSettings(), today)

        assertEquals(10, display.month)
        assertEquals(72_490, display.totalKrw)
        assertEquals(
            listOf(
                WidgetPayment("spotify", "스포티파이", "GREEN", 0, Money(10_900, Currency.KRW)),
                WidgetPayment("netflix", "넷플릭스 프리미엄", "RED", 3, Money(17_000, Currency.KRW)),
            ),
            display.upcoming,
        )
        assertTrue(display.hasSubscriptions)
    }

    @Test
    fun `월 시작 기준일이 25일이면 10월 25일부터 센 기간이 10월이다`() {
        assertEquals(10, buildWidgetDisplay(SampleData.records, AppSettings(monthStartDay = 25), today).month)
    }

    @Test
    fun `구독이 없으면 빈 상태`() {
        val display = buildWidgetDisplay(emptyList(), AppSettings(), today)

        assertFalse(display.hasSubscriptions)
        assertTrue(display.upcoming.isEmpty())
        assertEquals(0, display.totalKrw)
    }
}
