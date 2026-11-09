package jpark.bro.subcal.core.domain.billing

import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.TrialOutcome
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class BillingSummaryTest {
    private val today = date("2026-06-15")
    private val rate = 1_400L

    @Test
    fun `달러는 환율로 바꿔 원 단위 반올림한다`() {
        assertEquals(13_986, Money(999, Currency.USD).toKrw(rate))
        assertEquals(1, Money(1, Currency.USD).toKrw(50))
        assertEquals(0, Money(1, Currency.USD).toKrw(49))
        assertEquals(13_900, Money(13_900, Currency.KRW).toKrw(rate))
    }

    @Test
    fun `주기별 한 달 환산액`() {
        fun equivalent(
            unit: CycleUnit,
            interval: Int,
            amount: Long,
        ) = subscription(amount = amount, unit = unit, interval = interval)
            .record()
            .monthlyEquivalentKrw(today, rate)

        assertEquals(10_000, equivalent(CycleUnit.MONTH, 1, 10_000))
        assertEquals(3_333, equivalent(CycleUnit.MONTH, 3, 10_000))
        assertEquals(3_700, equivalent(CycleUnit.YEAR, 1, 44_400))
        assertEquals(1_850, equivalent(CycleUnit.YEAR, 2, 44_400))
        assertEquals(43_333, equivalent(CycleUnit.WEEK, 1, 10_000))
        assertEquals(21_667, equivalent(CycleUnit.WEEK, 2, 10_000))
        assertEquals(30_417, equivalent(CycleUnit.DAY, 1, 1_000))
    }

    @Test
    fun `한 달 환산은 오늘 유효한 금액을 쓴다`() {
        val record = subscription(amount = 10_000).record(priceChanges = listOf(priceChange("2026-06-15", 12_000)))

        assertEquals(12_000, record.monthlyEquivalentKrw(today, rate))
    }

    @Test
    fun `달러 구독 합계는 원화로 바꿔 더한다`() {
        val records = listOf(subscription(amount = 999).usd().record(), subscription().record())

        assertEquals(23_986, periodTotalKrw(records, BillingPeriod(YearMonth.of(2026, 6)), today, rate))
    }

    @Test
    fun `월 시작 기준일에 따라 같은 결제가 다른 기간에 들어간다`() {
        val records = listOf(subscription(unit = CycleUnit.YEAR, first = "2026-06-20").record())

        assertEquals(0, periodTotalKrw(records, BillingPeriod(YearMonth.of(2026, 6), 25), today, rate))
        assertEquals(10_000, periodTotalKrw(records, BillingPeriod(YearMonth.of(2026, 5), 25), today, rate))
    }

    @Test
    fun `일시정지와 해지 구독은 월 평균에서 빠진다`() {
        val records =
            listOf(
                subscription(name = "이용 중").record(),
                subscription(name = "정지").paused().record(
                    pauses = listOf(pause("2026-05-01")),
                ),
                subscription(name = "해지").cancelled("2026-05-01").record(),
            )

        assertEquals(10_000, monthlyAverageKrw(records, today, rate))
    }

    @Test
    fun `첫 결제일이 지났는데 고르지 않은 체험은 합계에 들어간다`() {
        val records =
            listOf(
                subscription(first = "2026-06-14").trial("2026-05-14", TrialOutcome.PENDING).record(),
            )

        assertEquals(10_000, periodTotalKrw(records, BillingPeriod(YearMonth.of(2026, 6)), today, rate))
        assertEquals(10_000, monthlyAverageKrw(records, today, rate))
    }

    @Test
    fun `누적 지출은 오늘 결제를 포함하고 대기 중 체험은 0`() {
        val record = subscription(first = "2026-04-15").record()
        val trial =
            subscription(first = "2026-06-15")
                .trial("2026-05-15", TrialOutcome.PENDING)
                .record()

        assertEquals(30_000, record.cumulativeSpendKrw(today, rate))
        assertEquals(0, trial.cumulativeSpendKrw(today, rate))
    }

    @Test
    fun `다가오는 결제는 날짜 다음 이름 순이고 개수를 제한한다`() {
        val records =
            listOf(
                subscription(name = "나", first = "2026-06-20").record(),
                subscription(name = "가", first = "2026-06-20").record(),
                subscription(name = "다", first = "2026-06-16").record(),
                subscription(name = "라", first = "2026-06-25").record(),
            )

        assertEquals(listOf("다", "가", "나"), upcomingPayments(records, today, limit = 3).map { it.subscription.name })
    }

    @Test
    fun `짧은 주기 구독도 다가오는 결제에는 한 번만 나온다`() {
        val records =
            listOf(
                subscription(name = "매일", unit = CycleUnit.DAY, first = "2026-06-01").record(),
                subscription(name = "매달", first = "2026-06-20").record(),
            )

        assertEquals(listOf("매일", "매달"), upcomingPayments(records, today, limit = 3).map { it.subscription.name })
    }
}
