package jpark.bro.subcal.core.domain.reminder

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.billing.date
import jpark.bro.subcal.core.domain.billing.record
import jpark.bro.subcal.core.domain.billing.subscription
import jpark.bro.subcal.core.domain.billing.trial
import jpark.bro.subcal.core.domain.billing.usd
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.model.TrialOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

class MonthlySummaryTest {
    private val on = NotificationSettings(monthlySummaryEnabled = true)
    private val records =
        listOf(
            subscription(name = "넷플릭스", amount = 17_000, first = "2026-01-02").record(),
            subscription(name = "구글 원", amount = 44_400, unit = CycleUnit.YEAR, first = "2023-11-09").record(),
            subscription(name = "ChatGPT", amount = 2_000, first = "2026-01-20").usd().record(),
            // 11/12가 첫 결제일인 체험은 결과를 고르기 전이라 합계에서 뺀다
            subscription(name = "밀리의 서재", amount = 9_900, first = "2026-11-12")
                .trial("2026-10-13", TrialOutcome.PENDING)
                .record(),
        )

    @Test
    fun `기간 첫날에 이번 달 결제 합과 건수를 알린다`() {
        val summary = monthlySummary(records, AppSettings(), on, date("2026-11-01"))

        // 17,000 + 44,400 + $20 × 1,400 = 28,000
        assertEquals(MonthlySummary(BillingPeriod(YearMonth.of(2026, 11)), 89_400, 3), summary)
    }

    @Test
    fun `월 시작 기준일이 25일이면 25일에 알린다`() {
        val settings = AppSettings(monthStartDay = 25)

        assertNull(monthlySummary(records, settings, on, date("2026-11-01")))
        val summary = monthlySummary(records, settings, on, date("2026-10-25"))
        assertEquals(BillingPeriod(YearMonth.of(2026, 10), startDay = 25), summary?.period)
    }

    @Test
    fun `꺼져 있거나 첫날이 아니면 알리지 않는다`() {
        assertNull(monthlySummary(records, AppSettings(), NotificationSettings(), date("2026-11-01")))
        assertNull(monthlySummary(records, AppSettings(), on, date("2026-11-02")))
    }

    @Test
    fun `구독이 없어도 0원 0건으로 알린다`() {
        assertEquals(0L, monthlySummary(emptyList(), AppSettings(), on, date("2026-11-01"))?.totalKrw)
    }
}
