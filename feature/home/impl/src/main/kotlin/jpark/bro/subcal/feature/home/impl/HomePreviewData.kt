package jpark.bro.subcal.feature.home.impl

import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.PriceChange
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionRecord
import java.time.LocalDate

/** product-spec 9장 이용 중 구독 (오늘 = 2026.10.30) */
@Suppress("MagicNumber") // 샘플 데이터
internal fun previewHomeState(
    monthStartDay: Int = 1,
    empty: Boolean = false,
): HomeState {
    val records =
        if (empty) {
            emptyList()
        } else {
            listOf(
                preview("스포티파이", Category.MUSIC, 10_900, "2026-10-30", "GREEN"),
                preview("넷플릭스 프리미엄", Category.VIDEO, 13_500, "2024-07-02", "RED").copy(
                    priceChanges =
                        listOf(
                            PriceChange("p1", "넷플릭스 프리미엄", LocalDate.of(2026, 8, 2), 17_000, createdAt = 0),
                        ),
                ),
                preview("쿠팡 와우", Category.SHOPPING, 7_890, "2025-02-05", "ORANGE"),
                preview("구글 원 200GB", Category.CLOUD, 44_400, "2023-11-09", "BLUE", CycleUnit.YEAR),
                preview("유튜브 프리미엄", Category.VIDEO, 14_900, "2025-01-14", "PINK"),
                preview("티빙", Category.VIDEO, 10_900, "2026-03-21", "ORANGE"),
                preview("멜론", Category.MUSIC, 10_900, "2025-05-25", "GREEN"),
            )
        }
    return buildHomeState(records, AppSettings(monthStartDay = monthStartDay), LocalDate.of(2026, 10, 30))
}

@Suppress("LongParameterList") // 샘플 데이터
private fun preview(
    name: String,
    category: Category,
    amount: Long,
    first: String,
    colorKey: String,
    unit: CycleUnit = CycleUnit.MONTH,
): SubscriptionRecord =
    SubscriptionRecord(
        Subscription(
            id = name,
            name = name,
            category = category,
            amount = Money(amount, Currency.KRW),
            cycle = BillingCycle(unit),
            firstBillingDate = LocalDate.parse(first),
            colorKey = colorKey,
            createdAt = 0,
            updatedAt = 0,
        ),
    )
