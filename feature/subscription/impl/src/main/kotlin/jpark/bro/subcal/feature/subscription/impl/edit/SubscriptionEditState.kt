package jpark.bro.subcal.feature.subscription.impl.edit

import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.PresetPlan
import jpark.bro.subcal.core.model.ReminderMode
import java.time.LocalDate

internal data class SubscriptionEditState(
    val isLoading: Boolean = true,
    /** 수정이면 "구독 수정" 제목과 삭제 버튼 */
    val isEditMode: Boolean = false,
    val presetId: String? = null,
    val plans: List<PresetPlan> = emptyList(),
    val selectedPlanIndex: Int? = null,
    /** 프리셋 요금 기준 월 yyyy.MM. 프리셋으로 채웠을 때만 */
    val pricesAsOf: String? = null,
    val name: String = "",
    /** SubCalAmountField 입력 원문 (쉼표 없음) */
    val amountInput: String = "",
    val currency: Currency = Currency.KRW,
    val cycleOption: CycleOption = CycleOption.MONTHLY,
    /** 직접 주기의 단위와 간격 */
    val customUnit: CycleUnit = CycleUnit.MONTH,
    val customIntervalInput: String = MIN_CUSTOM_INTERVAL.toString(),
    /** 체험이면 체험 종료일 */
    val firstBillingDate: LocalDate = LocalDate.ofEpochDay(0),
    /** null이면 무료체험이 아니다 */
    val trialStartedOn: LocalDate? = null,
    /** 첫 결제일이 지난 날이면 오늘까지 생긴 결제 횟수 (4-3 미리 보기), 아니면 0 */
    val pastPaymentCount: Int = 0,
    val paymentMethod: String = "",
    /** 다른 구독에서 쓴 결제 수단 중 지금 입력과 맞는 것 */
    val paymentMethodSuggestions: List<String> = emptyList(),
    val category: Category = Category.ETC,
    val reminder: ReminderOption = ReminderOption.DEFAULT,
    val colorKey: String = DEFAULT_COLOR_KEY,
    val cancelUrl: String = "",
    val memo: String = "",
    /** 사용 체크 (product-spec 4-10) */
    val trackUsage: Boolean = false,
    val datePicker: DateTarget? = null,
    val sheet: EditSheet? = null,
    val isSaving: Boolean = false,
    /** 이름이 있고 금액 > 0 이고 주기 · 체험 기간이 올바르면 (4-3 저장 조건) */
    val canSave: Boolean = false,
)

internal const val MAX_NAME_LENGTH = 30
internal const val MIN_CUSTOM_INTERVAL = 2
internal const val DEFAULT_COLOR_KEY = "BLUE"

/** 결제 주기 칩 (product-spec 2-2) */
internal enum class CycleOption {
    MONTHLY,
    YEARLY,
    WEEKLY,
    CUSTOM,
}

/** 구독별 알림 (product-spec 2-12). DEFAULT는 전역 설정을 따른다 */
@Suppress("MagicNumber") // 알림 일수 0 · 1 · 3 · 7
internal enum class ReminderOption(
    val mode: ReminderMode,
    val daysBefore: Int?,
) {
    DEFAULT(ReminderMode.DEFAULT, null),
    OFF(ReminderMode.OFF, null),
    SAME_DAY(ReminderMode.CUSTOM, 0),
    ONE_DAY(ReminderMode.CUSTOM, 1),
    THREE_DAYS(ReminderMode.CUSTOM, 3),
    SEVEN_DAYS(ReminderMode.CUSTOM, 7),
}

internal enum class DateTarget {
    FIRST_BILLING,
    TRIAL_START,
}

internal enum class EditSheet {
    /** 금액을 바꿨을 때 "언제부터 적용할까요?" (2-5) */
    PRICE_CHANGE,

    /** 삭제 확인 (2-6) */
    DELETE,
}

/** 고른 칩의 결제 주기. 직접 주기의 간격이 범위를 벗어나면 null */
internal fun SubscriptionEditState.billingCycle(): BillingCycle? =
    when (cycleOption) {
        CycleOption.MONTHLY -> {
            BillingCycle(CycleUnit.MONTH)
        }

        CycleOption.YEARLY -> {
            BillingCycle(CycleUnit.YEAR)
        }

        CycleOption.WEEKLY -> {
            BillingCycle(CycleUnit.WEEK)
        }

        CycleOption.CUSTOM -> {
            customIntervalInput
                .toIntOrNull()
                ?.takeIf { it in MIN_CUSTOM_INTERVAL..BillingCycle.MAX_INTERVAL }
                ?.let { BillingCycle(customUnit, it) }
        }
    }

/** 저장한 주기를 칩으로. 간격 1의 일 단위도 직접 주기로 보여준다 */
internal fun BillingCycle.toOption(): CycleOption =
    when {
        interval == 1 && unit == CycleUnit.MONTH -> CycleOption.MONTHLY
        interval == 1 && unit == CycleUnit.YEAR -> CycleOption.YEARLY
        interval == 1 && unit == CycleUnit.WEEK -> CycleOption.WEEKLY
        else -> CycleOption.CUSTOM
    }

internal fun reminderOptionOf(
    mode: ReminderMode,
    daysBefore: Int?,
): ReminderOption =
    ReminderOption.entries.firstOrNull { it.mode == mode && it.daysBefore == daysBefore } ?: ReminderOption.DEFAULT
