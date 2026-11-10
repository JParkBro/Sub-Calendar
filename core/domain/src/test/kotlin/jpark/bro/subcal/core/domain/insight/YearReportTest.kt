package jpark.bro.subcal.core.domain.insight

import jpark.bro.subcal.core.domain.billing.cancelled
import jpark.bro.subcal.core.domain.billing.date
import jpark.bro.subcal.core.domain.billing.priceChange
import jpark.bro.subcal.core.domain.billing.record
import jpark.bro.subcal.core.domain.billing.subscription
import jpark.bro.subcal.core.domain.billing.trial
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.PartyMember
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.TrialOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** product-spec 4-11, 9장 샘플 데이터 (오늘 = 2026.10.30, 환율 1,400원) */
class YearReportTest {
    private val today = date("2026-10-30")
    private val rate = 1_400L

    private fun member(name: String) =
        PartyMember(name, "유튜브 프리미엄", name, null, sortOrder = 0, createdAt = 0, updatedAt = 0)

    private val spotify =
        subscription(name = "스포티파이", amount = 10_900, first = "2026-10-30")
            .copy(category = Category.MUSIC)
            .trial("2026-09-30", TrialOutcome.KEPT)
            .record()
    private val netflix =
        subscription(name = "넷플릭스 프리미엄", amount = 13_500, first = "2024-07-02")
            .copy(category = Category.VIDEO)
            .record(priceChanges = listOf(priceChange("2026-08-02", 17_000)))
    private val coupang =
        subscription(name = "쿠팡 와우", amount = 7_890, first = "2025-02-05").copy(category = Category.SHOPPING).record()
    private val googleOne =
        subscription(name = "구글 원 200GB", amount = 44_400, unit = CycleUnit.YEAR, first = "2023-11-09")
            .copy(category = Category.CLOUD)
            .record()
    private val youtube =
        subscription(name = "유튜브 프리미엄", amount = 14_900, first = "2025-01-14")
            .copy(category = Category.VIDEO)
            .record()
            .copy(partyMembers = listOf(member("민지"), member("준호"), member("서연")))
    private val tving =
        subscription(name = "티빙", amount = 10_900, first = "2026-03-21").copy(category = Category.VIDEO).record()
    private val melon =
        subscription(name = "멜론", amount = 10_900, first = "2025-05-25").copy(category = Category.MUSIC).record()
    private val disney =
        subscription(name = "디즈니+ 프리미엄", amount = 13_900, first = "2026-10-31")
            .copy(category = Category.VIDEO)
            .trial("2026-10-01", TrialOutcome.PENDING)
            .record()
    private val watcha =
        subscription(name = "왓챠", amount = 7_900, first = "2026-08-31")
            .copy(category = Category.VIDEO)
            .trial("2026-08-01", TrialOutcome.CANCELLED)
            .cancelled("2026-08-30")
            .record()

    private val records: List<SubscriptionRecord> =
        listOf(spotify, netflix, coupang, googleOne, youtube, tving, melon, disney, watcha)

    @Test
    fun `올해 총 지출은 12월 31일까지 예정 결제를 넣은 내 몫 합이다`() {
        val report = yearReport(records, 2026, today, rate)

        // 스포티파이 32,700 + 넷플릭스 179,500 + 쿠팡 94,680 + 구글 원 44,400 + 유튜브 내 몫 44,700 + 티빙 109,000 + 멜론 130,800
        assertEquals(635_780, report.totalKrw)
        assertEquals(52_982, report.monthlyAverageKrw)
        assertEquals(12, report.monthlyKrw.size)
        assertEquals(61_315, report.monthlyKrw[9])
        assertEquals(report.totalKrw, report.monthlyKrw.sum())
    }

    @Test
    fun `카테고리 1위는 그 해 결제 합이 가장 큰 카테고리이고 비율은 내림한다`() {
        val top = yearReport(records, 2026, today, rate).topCategory

        assertEquals(TopCategory(Category.VIDEO, 333_200, 52, 3), top)
    }

    @Test
    fun `가장 오래 쓴 구독은 그 해 결제가 있던 구독 중 첫 결제일이 가장 이른 것이다`() {
        val longest = yearReport(records, 2026, today, rate).longest

        assertEquals(googleOne, longest?.record)
        assertEquals(4, longest?.yearCount)
    }

    @Test
    fun `올해 아낀 돈은 해지하지 않았다면 그 해에 생겼을 결제의 합이다`() {
        val report = yearReport(records, 2026, today, rate)

        // 왓챠 8/31 · 9/30 · 10/31 · 11/30 · 12/31
        assertEquals(39_500, report.savedKrw)
        assertEquals(1, report.savedCount)
    }

    @Test
    fun `지난해 리포트는 그 해 결제만 센다`() {
        val report = yearReport(records, 2025, today, rate)

        // 넷플릭스 162,000 + 쿠팡 86,790 + 구글 원 44,400 + 유튜브 내 몫 44,700 + 멜론 87,200
        assertEquals(425_090, report.totalKrw)
        assertEquals(3, report.longest?.yearCount)
        assertEquals(0, report.savedKrw)
    }

    @Test
    fun `12월 31일 결제는 넣고 다음 해 1월 1일 결제는 넣지 않는다`() {
        val daily = subscription(amount = 1_000, unit = CycleUnit.DAY, first = "2026-12-30").record()

        val report = yearReport(listOf(daily), 2026, today, rate)

        assertEquals(2_000, report.totalKrw)
        assertEquals(2_000, report.monthlyKrw[11])
    }

    @Test
    fun `지난해에 해지한 구독은 올해 1월 1일부터 아낀 돈을 센다`() {
        val cancelled =
            subscription(name = "해지", amount = 10_000, first = "2025-01-10").cancelled("2025-06-10").record()

        val report = yearReport(listOf(cancelled), 2026, today, rate)

        assertEquals(0, report.totalKrw)
        assertEquals(120_000, report.savedKrw)
        assertNull(report.longest)
        assertEquals(false, report.isEmpty)
    }

    @Test
    fun `해지한 해에는 해지 전 결제가 지출, 해지 후 결제가 아낀 돈이다`() {
        val cancelled =
            subscription(name = "해지", amount = 10_000, first = "2024-01-10").cancelled("2026-06-10").record()

        val report = yearReport(listOf(cancelled), 2026, today, rate)

        assertEquals(50_000, report.totalKrw)
        assertEquals(70_000, report.savedKrw)
        assertEquals(3, report.longest?.yearCount)
    }

    @Test
    fun `결제도 아낀 돈도 없으면 비어 있다`() {
        val report = yearReport(emptyList(), 2026, today, rate)

        assertTrue(report.isEmpty)
        assertNull(report.topCategory)
        assertNull(report.longest)
        assertEquals(0, report.monthlyAverageKrw)
    }

    @Test
    fun `금액이 같은 카테고리는 카테고리 순서가 앞인 것이 1위다`() {
        val music = subscription(name = "음악", amount = 5_000).copy(category = Category.MUSIC).record()
        val video = subscription(name = "영상", amount = 5_000).copy(category = Category.VIDEO).record()

        val top = yearReport(listOf(music, video), 2026, today, rate).topCategory

        assertEquals(Category.VIDEO, top?.category)
        assertEquals(50, top?.percent)
    }

    @Test
    fun `배너는 12월에 올해, 1월에 지난해 리포트를 띄운다`() {
        assertEquals(2026, yearReportBannerYear(date("2026-12-01")))
        assertEquals(2026, yearReportBannerYear(date("2026-12-31")))
        assertEquals(2026, yearReportBannerYear(date("2027-01-31")))
        assertNull(yearReportBannerYear(date("2026-11-30")))
        assertNull(yearReportBannerYear(date("2027-02-01")))
    }
}
