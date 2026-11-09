package jpark.bro.subcal.feature.subscription.impl.detail

import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Pause
import jpark.bro.subcal.core.model.PriceChange
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import java.time.LocalDate

/** product-spec 9장 넷플릭스 (오늘 = 2026.10.30). [paused]면 10.05부터 일시정지 */
@Suppress("MagicNumber") // 샘플 데이터
internal fun previewDetailState(paused: Boolean = false): SubscriptionDetailState {
    val today = LocalDate.of(2026, 10, 30)
    val record =
        SubscriptionRecord(
            subscription =
                Subscription(
                    id = "netflix",
                    name = "넷플릭스 프리미엄",
                    planName = "프리미엄",
                    category = Category.VIDEO,
                    amount = Money(13_500, Currency.KRW),
                    cycle = BillingCycle(CycleUnit.MONTH),
                    firstBillingDate = LocalDate.of(2024, 7, 2),
                    paymentMethod = "신한카드",
                    status = if (paused) SubscriptionStatus.PAUSED else SubscriptionStatus.ACTIVE,
                    colorKey = "RED",
                    memo = "가족 공유 X (개인 프로필 전용)",
                    createdAt = 0,
                    updatedAt = 0,
                ),
            priceChanges = listOf(PriceChange("p1", "netflix", LocalDate.of(2026, 8, 2), 17_000, createdAt = 0)),
            pauses =
                if (paused) {
                    listOf(
                        Pause("pause1", "netflix", LocalDate.of(2026, 10, 5), createdAt = 0),
                    )
                } else {
                    emptyList()
                },
        )
    return SubscriptionDetailState(
        isLoading = false,
        today = today,
        content = buildDetailContent(record, today, usdKrwRate = 1_400),
    )
}
