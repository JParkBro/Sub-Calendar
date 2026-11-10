package jpark.bro.subcal.core.domain.insight

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.billing.cancelled
import jpark.bro.subcal.core.domain.billing.date
import jpark.bro.subcal.core.domain.billing.pause
import jpark.bro.subcal.core.domain.billing.priceChange
import jpark.bro.subcal.core.domain.billing.record
import jpark.bro.subcal.core.domain.billing.subscription
import jpark.bro.subcal.core.domain.billing.trial
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.TrialOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

/** product-spec 9장 샘플 (오늘 = 2026.10.30, 환율 1,400원) */
class InsightsTest {
    private val today = date("2026-10-30")
    private val rate = 1_400L

    private fun SubscriptionRecord.category(category: Category) =
        copy(subscription = subscription.copy(category = category))

    private fun SubscriptionRecord.considerCancel() = copy(subscription = subscription.copy(considerCancel = true))

    private val spotify =
        subscription(name = "스포티파이", amount = 10_900, first = "2026-10-30")
            .trial("2026-09-30", TrialOutcome.KEPT)
            .record()
            .category(Category.MUSIC)
    private val netflix =
        subscription(name = "넷플릭스 프리미엄", amount = 13_500, first = "2024-07-02")
            .record(priceChanges = listOf(priceChange("2026-08-02", 17_000)))
            .category(Category.VIDEO)
    private val coupang =
        subscription(name = "쿠팡 와우", amount = 7_890, first = "2025-02-05").record().category(Category.SHOPPING)
    private val googleOne =
        subscription(name = "구글 원 200GB", amount = 44_400, unit = CycleUnit.YEAR, first = "2023-11-09")
            .record()
            .category(Category.CLOUD)
    private val youtube =
        subscription(name = "유튜브 프리미엄", amount = 14_900, first = "2025-01-14").record().category(Category.VIDEO)
    private val tving =
        subscription(name = "티빙", amount = 10_900, first = "2026-03-21")
            .record()
            .category(Category.VIDEO)
            .considerCancel()
    private val melon =
        subscription(name = "멜론", amount = 10_900, first = "2025-05-25")
            .record()
            .category(Category.MUSIC)
            .considerCancel()
    private val disney =
        subscription(name = "디즈니+ 프리미엄", amount = 13_900, first = "2026-10-31")
            .trial("2026-10-01", TrialOutcome.PENDING)
            .record()
            .category(Category.VIDEO)
    private val watcha =
        subscription(name = "왓챠", amount = 7_900, first = "2026-08-31")
            .trial("2026-08-01", TrialOutcome.CANCELLED)
            .cancelled(on = "2026-08-30")
            .record()
            .category(Category.VIDEO)

    private val records = listOf(spotify, netflix, coupang, googleOne, youtube, tving, melon, disney, watcha)

    @Test
    fun `카테고리 비중은 이용 중 구독의 월 환산을 큰 순으로 더하고 비율 합은 100이다`() {
        val shares = categoryShares(records, today, rate)

        assertEquals(
            listOf(
                CategoryShare(Category.VIDEO, 42_800, 56),
                CategoryShare(Category.MUSIC, 21_800, 29),
                CategoryShare(Category.SHOPPING, 7_890, 10),
                CategoryShare(Category.CLOUD, 3_700, 5),
            ),
            shares,
        )
        assertEquals(76_190, shares.sumOf { it.monthlyKrw })
    }

    @Test
    fun `비중은 같은 비율이면 앞 항목에 1%를 더해 합을 100으로 맞추고 구독이 없으면 비어 있다`() {
        val three =
            listOf(Category.VIDEO, Category.MUSIC, Category.CLOUD).map {
                subscription(name = it.name, amount = 10_000, first = "2026-01-01").record().category(it)
            }

        assertEquals(listOf(34, 33, 33), categoryShares(three, today, rate).map { it.percent })
        assertTrue(categoryShares(emptyList(), today, rate).isEmpty())
    }

    @Test
    fun `최근 12개 기간 합은 오래된 순이고 마지막이 이번 달이다`() {
        val totals = recentPeriodTotals(records, BillingPeriod(YearMonth.of(2026, 10)), today, rate, count = 12)

        assertEquals(12, totals.size)
        assertEquals(YearMonth.of(2025, 11), totals.first().period.month)
        assertEquals(PeriodTotal(BillingPeriod(YearMonth.of(2026, 9)), 61_590), totals[10])
        assertEquals(PeriodTotal(BillingPeriod(YearMonth.of(2026, 10)), 72_490), totals.last())
    }

    @Test
    fun `월 시작 기준일이 25일이면 기간도 25일부터 센다`() {
        val totals =
            recentPeriodTotals(listOf(melon), BillingPeriod(YearMonth.of(2026, 10), startDay = 25), today, rate, 2)

        // 멜론 25일 결제: 9월 기간(9.25 ~ 10.24)과 10월 기간(10.25 ~ 11.24)에 한 번씩
        assertEquals(listOf(10_900L, 10_900L), totals.map { it.totalKrw })
    }

    @Test
    fun `아낀 돈은 해지일부터 오늘까지 생겼을 결제의 합이다`() {
        // 왓챠: 8/30 해지, 8/31 · 9/30(말일 규칙) = 15,800원. 10/31은 아직 오지 않음
        assertEquals(15_800, watcha.savedKrw(today, rate))
        assertEquals(15_800, totalSavedKrw(records, today, rate))
        assertEquals(0, netflix.savedKrw(today, rate))
    }

    @Test
    fun `해지일 당일 결제는 아낀 돈에 들어가고 일시정지 기간과 가격 변경을 반영한다`() {
        val record =
            subscription(amount = 10_000, first = "2026-01-15")
                .cancelled(on = "2026-08-15")
                .record(
                    priceChanges = listOf(priceChange("2026-10-15", 12_000)),
                    pauses = listOf(pause("2026-09-01", "2026-09-30")),
                )

        // 8/15(해지일 당일) 10,000 + 9/15 일시정지로 없음 + 10/15 12,000
        assertEquals(22_000, record.savedKrw(today, rate))
    }

    @Test
    fun `해지하면 연 n원은 해지 고민함 이용 중 구독의 연간 환산 합이다`() {
        assertEquals(listOf(tving, melon), considerCancelRecords(records, today))
        assertEquals(261_600, considerCancelAnnualKrw(records, today, rate))
    }
}
