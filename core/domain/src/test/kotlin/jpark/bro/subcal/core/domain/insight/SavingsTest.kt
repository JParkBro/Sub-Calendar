package jpark.bro.subcal.core.domain.insight

import jpark.bro.subcal.core.domain.billing.cancelled
import jpark.bro.subcal.core.domain.billing.date
import jpark.bro.subcal.core.domain.billing.paused
import jpark.bro.subcal.core.domain.billing.record
import jpark.bro.subcal.core.domain.billing.subscription
import jpark.bro.subcal.core.domain.billing.trial
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Preset
import jpark.bro.subcal.core.model.PresetCatalog
import jpark.bro.subcal.core.model.PresetPlan
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.TrialOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** product-spec 9장 샘플 (오늘 = 2026.10.30, 환율 1,400원) */
class SavingsTest {
    private val today = date("2026-10-30")
    private val rate = 1_400L

    private fun record(
        name: String,
        category: Category,
        amount: Long = 10_000,
        first: String = "2026-01-15",
    ): SubscriptionRecord =
        subscription(name = name, amount = amount, first = first)
            .copy(category = category)
            .record()

    @Test
    fun `중복은 영상 · 음악 · 클라우드 · AI에서 이용 중 구독이 2개 이상일 때 월 환산 합이 큰 순`() {
        val records =
            listOf(
                record("넷플릭스 프리미엄", Category.VIDEO, 17_000),
                record("유튜브 프리미엄", Category.VIDEO, 14_900),
                record("티빙", Category.VIDEO, 10_900),
                record("스포티파이", Category.MUSIC, 10_900),
                record("멜론", Category.MUSIC, 10_900),
                record("쿠팡 와우", Category.SHOPPING, 7_890),
                record("컬리멤버스", Category.SHOPPING, 1_900),
                record("구글 원", Category.CLOUD, 2_400),
            )

        val groups = duplicateGroups(records, today, rate)

        assertEquals(listOf(Category.VIDEO, Category.MUSIC), groups.map { it.category })
        assertEquals(42_800, groups[0].monthlyKrw)
        assertEquals(listOf("넷플릭스 프리미엄", "유튜브 프리미엄", "티빙"), groups[0].records.map { it.subscription.name })
        assertEquals(21_800, groups[1].monthlyKrw)
    }

    @Test
    fun `일시정지 · 해지 · 체험 결과 대기 구독은 중복으로 세지 않는다`() {
        val records =
            listOf(
                record("스포티파이", Category.MUSIC),
                subscription(name = "멜론").copy(category = Category.MUSIC).paused().record(),
                subscription(name = "지니").copy(category = Category.MUSIC).cancelled(on = "2026-09-01").record(),
                subscription(name = "플로", first = "2026-11-10")
                    .copy(category = Category.MUSIC)
                    .trial("2026-10-10", TrialOutcome.PENDING)
                    .record(),
            )

        assertTrue(duplicateGroups(records, today, rate).isEmpty())
    }

    @Test
    fun `해지한 구독별 아낀 돈은 큰 순이다`() {
        val watcha =
            subscription(name = "왓챠", amount = 7_900, first = "2026-08-31")
                .trial("2026-08-01", TrialOutcome.CANCELLED)
                .cancelled(on = "2026-08-30")
                .record()
        val wavve =
            subscription(
                name = "웨이브",
                amount = 7_900,
                first = "2026-01-10",
            ).cancelled(on = "2026-06-01").record()

        val items = savedItems(listOf(watcha, wavve, record("넷플릭스", Category.VIDEO)), today, rate)

        // 웨이브: 6/10 ~ 10/10 다섯 번 = 39,500원, 왓챠: 15,800원
        assertEquals(
            listOf("웨이브" to 39_500L, "왓챠" to 15_800L),
            items.map { it.record.subscription.name to it.savedKrw },
        )
    }

    @Test
    fun `같은 이름의 연간 요금제가 있으면 1년 차액을 보여준다`() {
        val youtube =
            subscription(name = "유튜브 프리미엄", amount = 14_900)
                .copy(presetId = "youtube_premium", planName = "개인")
                .record()
        val catalog =
            catalog(
                plan("개인", 14_900, CycleUnit.MONTH),
                plan("개인 연간", 149_000, CycleUnit.YEAR),
                plan("가족 연간", 299_000, CycleUnit.YEAR),
            )

        val options = annualPlanOptions(listOf(youtube), catalog, today, rate)

        assertEquals(1, options.size)
        assertEquals("개인 연간", options.single().annualPlan.name)
        assertEquals(29_800, options.single().yearlySavingKrw)
    }

    @Test
    fun `연간 요금제가 없거나 등급을 고를 수 없거나 더 비싸면 보여주지 않는다`() {
        val monthly =
            subscription(name = "유튜브 프리미엄", amount = 14_900)
                .copy(presetId = "youtube_premium", planName = "가족")
                .record()

        assertTrue(
            annualPlanOptions(listOf(monthly), catalog(plan("개인", 14_900, CycleUnit.MONTH)), today, rate).isEmpty(),
        )
        assertTrue(
            annualPlanOptions(
                listOf(monthly),
                catalog(plan("개인 연간", 149_000, CycleUnit.YEAR), plan("학생 연간", 99_000, CycleUnit.YEAR)),
                today,
                rate,
            ).isEmpty(),
        )
        assertTrue(
            annualPlanOptions(listOf(monthly), catalog(plan("연간", 200_000, CycleUnit.YEAR)), today, rate).isEmpty(),
        )
    }

    private fun plan(
        name: String,
        amount: Long,
        unit: CycleUnit,
    ) = PresetPlan(name, Money(amount, Currency.KRW), BillingCycle(unit))

    private fun catalog(vararg plans: PresetPlan) =
        PresetCatalog(
            version = 1,
            pricesAsOf = "2026.10",
            services = listOf(Preset("youtube_premium", "유튜브 프리미엄", Category.VIDEO, "RED", plans.toList())),
        )
}
