package jpark.bro.subcal.core.testing

import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Preset
import jpark.bro.subcal.core.model.PresetCatalog
import jpark.bro.subcal.core.model.PresetPlan
import jpark.bro.subcal.core.model.PriceChange
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.model.TrialOutcome
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

/** product-spec 9장 샘플 데이터 시나리오. 오늘 = 2026.10.30 (금), 월 시작 기준일 1일, 달러 환율 1,400원 */
@Suppress("MagicNumber", "LongParameterList") // 샘플 데이터
object SampleData {
    val today: LocalDate = LocalDate.of(2026, 10, 30)

    /** [today] 정오(서울)에 멈춘 시계 */
    val clock: Clock =
        Clock.fixed(today.atTime(12, 0).atZone(SEOUL).toInstant(), SEOUL)

    val spotify: SubscriptionRecord =
        record(
            id = "spotify",
            name = "스포티파이",
            category = Category.MUSIC,
            amount = 10_900,
            first = "2026-10-30",
            colorKey = "GREEN",
        ) { copy(trialStartedOn = LocalDate.parse("2026-09-30"), trialOutcome = TrialOutcome.KEPT) }

    val netflix: SubscriptionRecord =
        record(
            id = "netflix",
            name = "넷플릭스 프리미엄",
            category = Category.VIDEO,
            amount = 13_500,
            first = "2024-07-02",
            colorKey = "RED",
        ).copy(
            priceChanges =
                listOf(
                    PriceChange(
                        id = "netflix-price-1",
                        subscriptionId = "netflix",
                        effectiveFrom = LocalDate.parse("2026-08-02"),
                        amountMinor = 17_000,
                        createdAt = 0,
                    ),
                ),
        )

    val coupangWow: SubscriptionRecord =
        record(
            id = "coupang_wow",
            name = "쿠팡 와우",
            category = Category.SHOPPING,
            amount = 7_890,
            first = "2025-02-05",
            colorKey = "ORANGE",
        )

    val googleOne: SubscriptionRecord =
        record(
            id = "google_one",
            name = "구글 원 200GB",
            category = Category.CLOUD,
            amount = 44_400,
            first = "2023-11-09",
            colorKey = "BLUE",
            unit = CycleUnit.YEAR,
        )

    val youtubePremium: SubscriptionRecord =
        record(
            id = "youtube_premium",
            name = "유튜브 프리미엄",
            category = Category.VIDEO,
            amount = 14_900,
            first = "2025-01-14",
            colorKey = "PINK",
        )

    val tving: SubscriptionRecord =
        record(
            id = "tving",
            name = "티빙",
            category = Category.VIDEO,
            amount = 10_900,
            first = "2026-03-21",
            colorKey = "ORANGE",
        ) { copy(considerCancel = true, trackUsage = true) }

    val melon: SubscriptionRecord =
        record(
            id = "melon",
            name = "멜론",
            category = Category.MUSIC,
            amount = 10_900,
            first = "2025-05-25",
            colorKey = "GREEN",
        ) { copy(considerCancel = true) }

    val disneyPlus: SubscriptionRecord =
        record(
            id = "disney_plus",
            name = "디즈니+ 프리미엄",
            category = Category.VIDEO,
            amount = 13_900,
            first = "2026-10-31",
            colorKey = "BLACK",
        ) { copy(trialStartedOn = LocalDate.parse("2026-10-01"), trialOutcome = TrialOutcome.PENDING) }

    val millie: SubscriptionRecord =
        record(
            id = "millie",
            name = "밀리의 서재",
            category = Category.READING,
            amount = 9_900,
            first = "2026-11-12",
            colorKey = "AMBER",
        ) { copy(trialStartedOn = LocalDate.parse("2026-10-13"), trialOutcome = TrialOutcome.PENDING) }

    val watcha: SubscriptionRecord =
        record(
            id = "watcha",
            name = "왓챠",
            category = Category.VIDEO,
            amount = 7_900,
            first = "2026-08-31",
            colorKey = "PINK",
        ) {
            copy(
                trialStartedOn = LocalDate.parse("2026-08-01"),
                trialOutcome = TrialOutcome.CANCELLED,
                status = SubscriptionStatus.CANCELLED,
                cancelledOn = LocalDate.parse("2026-08-30"),
            )
        }

    /** 테스트용 프리셋 5개. 요금은 9장 샘플에 맞춘 값이고 실제 프리셋 JSON과 다를 수 있다 */
    val catalog: PresetCatalog =
        PresetCatalog(
            version = 1,
            pricesAsOf = "2026.10",
            services =
                listOf(
                    preset(
                        "netflix",
                        "넷플릭스",
                        Category.VIDEO,
                        "RED",
                        plan("광고형 스탠다드", 7_000),
                        plan("스탠다드", 13_500),
                        plan("프리미엄", 17_000),
                    ),
                    preset("disney_plus", "디즈니+", Category.VIDEO, "BLACK", plan("스탠다드", 9_900), plan("프리미엄", 13_900)),
                    preset("watcha", "왓챠", Category.VIDEO, "PINK"),
                    preset("spotify", "스포티파이", Category.MUSIC, "GREEN", plan("프리미엄", 10_900)),
                    preset(
                        "chatgpt_plus",
                        "ChatGPT Plus",
                        Category.AI,
                        "TEAL",
                        PresetPlan("Plus", Money(2_000, Currency.USD), BillingCycle(CycleUnit.MONTH)),
                    ),
                ),
        )

    /** 이용 중 7개 + 체험 중 2개 + 해지 1개 */
    val records: List<SubscriptionRecord> =
        listOf(spotify, netflix, coupangWow, googleOne, youtubePremium, tving, melon, disneyPlus, millie, watcha)

    private fun preset(
        id: String,
        name: String,
        category: Category,
        colorKey: String,
        vararg plans: PresetPlan,
    ): Preset = Preset(id, name, category, colorKey, plans.toList(), cancelUrl = "https://example.com/$id/cancel")

    private fun plan(
        name: String,
        amount: Long,
    ): PresetPlan = PresetPlan(name, Money(amount, Currency.KRW), BillingCycle(CycleUnit.MONTH))

    private fun record(
        id: String,
        name: String,
        category: Category,
        amount: Long,
        first: String,
        colorKey: String,
        unit: CycleUnit = CycleUnit.MONTH,
        edit: Subscription.() -> Subscription = { this },
    ): SubscriptionRecord =
        SubscriptionRecord(
            Subscription(
                id = id,
                presetId = id,
                name = name,
                category = category,
                amount = Money(amount, Currency.KRW),
                cycle = BillingCycle(unit),
                firstBillingDate = LocalDate.parse(first),
                colorKey = colorKey,
                createdAt = 0,
                updatedAt = 0,
            ).edit(),
        )
}

private val SEOUL: ZoneId = ZoneId.of("Asia/Seoul")
