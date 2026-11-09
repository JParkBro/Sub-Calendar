package jpark.bro.subcal.core.model

import java.time.LocalDate

/**
 * 구독의 결제 규칙 (data-model-erd 2장). 결제 내역 · 누적 지출은 저장하지 않고 core:domain에서 계산한다.
 * soft delete된 행은 Repository가 걸러서 이 모델로 나오지 않는다.
 */
data class Subscription(
    val id: String,
    val presetId: String? = null,
    val name: String,
    val planName: String? = null,
    val category: Category = Category.ETC,
    val amount: Money,
    val cycle: BillingCycle,
    /** 체험이면 체험 종료일 */
    val firstBillingDate: LocalDate,
    /** null이면 체험이 아니다 */
    val trialStartedOn: LocalDate? = null,
    val trialOutcome: TrialOutcome = TrialOutcome.NONE,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    /** status가 CANCELLED일 때만 */
    val cancelledOn: LocalDate? = null,
    val paymentMethod: String? = null,
    val reminderMode: ReminderMode = ReminderMode.DEFAULT,
    /** reminderMode가 CUSTOM일 때만: 0, 1, 3, 7 */
    val reminderDaysBefore: Int? = null,
    /** 서비스 색 이름 (theme-guide 5장) */
    val colorKey: String,
    val cancelUrl: String? = null,
    val memo: String? = null,
    val considerCancel: Boolean = false,
    val trackUsage: Boolean = false,
    val partySplitMode: PartySplitMode = PartySplitMode.EQUAL,
    /** epoch millis */
    val createdAt: Long,
    /** epoch millis */
    val updatedAt: Long,
)

enum class SubscriptionStatus {
    ACTIVE,
    PAUSED,
    CANCELLED,
}

enum class TrialOutcome {
    NONE,
    PENDING,
    KEPT,
    CANCELLED,
}

enum class ReminderMode {
    DEFAULT,
    OFF,
    CUSTOM,
}

enum class PartySplitMode {
    EQUAL,
    CUSTOM,
}
