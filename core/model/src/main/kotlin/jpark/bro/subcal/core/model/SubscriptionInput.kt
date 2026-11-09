package jpark.bro.subcal.core.model

import java.time.LocalDate

/** 구독 입력 화면에서 사용자가 정하는 값. id · 상태 · 시각은 Repository가 채운다 (product-spec 2-1) */
data class SubscriptionInput(
    val presetId: String? = null,
    val name: String,
    val planName: String? = null,
    val category: Category = Category.ETC,
    val amount: Money,
    val cycle: BillingCycle,
    val firstBillingDate: LocalDate,
    /** null이면 체험이 아니다 */
    val trialStartedOn: LocalDate? = null,
    val paymentMethod: String? = null,
    val reminderMode: ReminderMode = ReminderMode.DEFAULT,
    /** reminderMode가 CUSTOM일 때만: 0, 1, 3, 7 */
    val reminderDaysBefore: Int? = null,
    val colorKey: String,
    val cancelUrl: String? = null,
    val memo: String? = null,
    val trackUsage: Boolean = false,
)

/** 금액을 바꿀 때 언제부터 적용할지 (product-spec 2-5) */
enum class PriceChangeTiming {
    NEXT_PAYMENT,
    FROM_START,
}
