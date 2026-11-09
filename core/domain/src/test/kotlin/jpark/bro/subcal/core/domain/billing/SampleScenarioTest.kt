package jpark.bro.subcal.core.domain.billing

import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.TrialOutcome
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

/** product-spec 9장 샘플 데이터의 기대 값 */
class SampleScenarioTest {
    private val today = date("2026-10-30")
    private val rate = 1_400L

    private val spotify =
        subscription(
            name = "스포티파이",
            amount = 10_900,
            first = "2026-10-30",
        ).trial("2026-09-30", TrialOutcome.KEPT).record()
    private val netflix =
        subscription(name = "넷플릭스 프리미엄", amount = 13_500, first = "2024-07-02")
            .record(priceChanges = listOf(priceChange("2026-08-02", 17_000)))
    private val coupang = subscription(name = "쿠팡 와우", amount = 7_890, first = "2025-02-05").record()
    private val googleOne =
        subscription(name = "구글 원 200GB", amount = 44_400, unit = CycleUnit.YEAR, first = "2023-11-09").record()
    private val youtube = subscription(name = "유튜브 프리미엄", amount = 14_900, first = "2025-01-14").record()
    private val tving = subscription(name = "티빙", amount = 10_900, first = "2026-03-21").record()
    private val melon = subscription(name = "멜론", amount = 10_900, first = "2025-05-25").record()
    private val disney =
        subscription(
            name = "디즈니+ 프리미엄",
            amount = 13_900,
            first = "2026-10-31",
        ).trial("2026-10-01", TrialOutcome.PENDING).record()
    private val millie =
        subscription(
            name = "밀리의 서재",
            amount = 9_900,
            first = "2026-11-12",
        ).trial("2026-10-13", TrialOutcome.PENDING).record()
    private val watcha =
        subscription(
            name = "왓챠",
            amount = 7_900,
            first = "2026-08-31",
        ).trial("2026-08-01", TrialOutcome.CANCELLED).cancelled("2026-08-30").record()

    private val records =
        listOf(spotify, netflix, coupang, googleOne, youtube, tving, melon, disney, millie, watcha)

    private fun total(month: Int) = periodTotalKrw(records, BillingPeriod(YearMonth.of(2026, month)), today, rate)

    @Test
    fun `10월 구독료`() {
        assertEquals(72_490, total(10))
    }

    @Test
    fun `9월 구독료와 지난달 대비`() {
        assertEquals(61_590, total(9))
        assertEquals(10_900, periodChangeKrw(records, BillingPeriod(YearMonth.of(2026, 10)), today, rate))
    }

    @Test
    fun `11월 구독료는 구글 원 연간을 포함하고 체험 2건을 뺀다`() {
        assertEquals(116_890, total(11))
    }

    @Test
    fun `11월 결제 목록은 날짜순 7건이고 체험 종료일은 결제가 아니다`() {
        val payments = periodPayments(records, BillingPeriod(YearMonth.of(2026, 11)), today)

        assertEquals(
            listOf("넷플릭스 프리미엄", "쿠팡 와우", "구글 원 200GB", "유튜브 프리미엄", "티빙", "멜론", "스포티파이"),
            payments.map { it.subscription.name },
        )
        assertEquals(listOf(2, 5, 9, 14, 21, 25, 30), payments.map { it.date.dayOfMonth })
    }

    @Test
    fun `월 평균과 연간 환산`() {
        assertEquals(76_190, monthlyAverageKrw(records, today, rate))
        assertEquals(914_280, annualizedKrw(records, today, rate))
    }

    @Test
    fun `다가오는 결제 3개`() {
        val upcoming = upcomingPayments(records, today, limit = 3)

        assertEquals(
            listOf("스포티파이" to date("2026-10-30"), "넷플릭스 프리미엄" to date("2026-11-02"), "쿠팡 와우" to date("2026-11-05")),
            upcoming.map { it.subscription.name to it.date },
        )
    }

    @Test
    fun `넷플릭스 누적 지출 28회`() {
        assertEquals(28, netflix.payments(netflix.subscription.firstBillingDate, today.plusDays(1)).size)
        assertEquals(388_500, netflix.cumulativeSpendKrw(today, rate))
    }

    @Test
    fun `체험 중 해지한 왓챠는 결제가 없다`() {
        assertEquals(0, watcha.cumulativeSpendKrw(today, rate))
    }

    @Test
    fun `샘플의 다음 결제일`() {
        fun next(record: SubscriptionRecord) = record.nextPayment(today)?.date?.toString()

        assertEquals("2026-11-09", next(googleOne))
        assertEquals("2026-11-14", next(youtube))
        assertEquals("2026-11-21", next(tving))
        assertEquals("2026-11-25", next(melon))
    }
}
