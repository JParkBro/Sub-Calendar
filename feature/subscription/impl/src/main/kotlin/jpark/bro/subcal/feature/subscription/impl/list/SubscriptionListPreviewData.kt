package jpark.bro.subcal.feature.subscription.impl.list

import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.ListSort
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionRecord
import java.time.LocalDate

/** product-spec 9장 샘플 중 이용 중 구독 (오늘 = 2026.10.30) */
@Suppress("MagicNumber") // 샘플 데이터
internal fun previewState(sort: ListSort = ListSort.NEXT_BILLING): SubscriptionListState {
    val records =
        listOf(
            preview("넷플릭스 프리미엄", Category.VIDEO, 17_000, "2024-07-02", "RED"),
            preview("쿠팡 와우", Category.SHOPPING, 7_890, "2025-02-05", "ORANGE"),
            preview("구글 원 200GB", Category.CLOUD, 44_400, "2023-11-09", "BLUE", CycleUnit.YEAR),
            preview("유튜브 프리미엄", Category.VIDEO, 14_900, "2025-01-14", "PINK", planName = "가족 요금제"),
            preview("티빙", Category.VIDEO, 10_900, "2026-03-21", "ORANGE"),
            preview("멜론", Category.MUSIC, 10_900, "2025-05-25", "GREEN"),
            preview("스포티파이", Category.MUSIC, 10_900, "2026-10-30", "GREEN"),
        )
    val today = LocalDate.of(2026, 10, 30)
    return SubscriptionListState(
        isLoading = false,
        sort = sort,
        content = buildListContent(records, ListFilter(), sort, today, usdKrwRate = 1_400),
    )
}

@Suppress("LongParameterList") // 샘플 데이터
private fun preview(
    name: String,
    category: Category,
    amount: Long,
    first: String,
    colorKey: String,
    unit: CycleUnit = CycleUnit.MONTH,
    planName: String? = null,
): SubscriptionRecord =
    SubscriptionRecord(
        Subscription(
            id = name,
            name = name,
            planName = planName,
            category = category,
            amount = Money(amount, Currency.KRW),
            cycle = BillingCycle(unit),
            firstBillingDate = LocalDate.parse(first),
            colorKey = colorKey,
            createdAt = 0,
            updatedAt = 0,
        ),
    )
