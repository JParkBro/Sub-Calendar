package jpark.bro.subcal.core.domain.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.YearMonth

class BillingPeriodTest {
    @Test
    fun `월 시작 25일이면 10월 기간은 10월 25일부터 11월 24일까지`() {
        val period = BillingPeriod(YearMonth.of(2026, 10), startDay = 25)

        assertEquals(date("2026-10-25"), period.start)
        assertEquals(date("2026-11-25"), period.endExclusive)
    }

    @Test
    fun `시작일 전날은 지난 기간이고 당일은 새 기간`() {
        assertEquals(YearMonth.of(2026, 9), BillingPeriod.containing(date("2026-10-24"), 25).month)
        assertEquals(YearMonth.of(2026, 10), BillingPeriod.containing(date("2026-10-25"), 25).month)
    }

    @Test
    fun `1월 초는 지난해 12월 기간`() {
        val period = BillingPeriod.containing(date("2027-01-10"), 25)

        assertEquals(date("2026-12-25"), period.start)
        assertEquals(date("2027-01-25"), period.endExclusive)
    }

    @Test
    fun `월 시작 1일이면 달력 월과 같다`() {
        val period = BillingPeriod.containing(date("2026-02-28"), 1)

        assertEquals(date("2026-02-01"), period.start)
        assertEquals(date("2026-03-01"), period.endExclusive)
    }

    @Test
    fun `지난 기간은 연도를 넘어간다`() {
        assertEquals(YearMonth.of(2026, 12), BillingPeriod(YearMonth.of(2027, 1)).previous().month)
    }

    @Test
    fun `월 시작일은 1부터 28까지만 허용`() {
        BillingPeriod(YearMonth.of(2026, 2), startDay = 28)
        assertThrows(IllegalArgumentException::class.java) { BillingPeriod(YearMonth.of(2026, 2), startDay = 29) }
        assertThrows(IllegalArgumentException::class.java) { BillingPeriod(YearMonth.of(2026, 2), startDay = 0) }
    }
}
