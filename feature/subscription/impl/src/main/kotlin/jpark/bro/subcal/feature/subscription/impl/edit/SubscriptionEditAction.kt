package jpark.bro.subcal.feature.subscription.impl.edit

import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.PriceChangeTiming
import java.time.LocalDate

internal sealed interface SubscriptionEditAction {
    /** 요금제 · 금액 · 주기 입력 */
    sealed interface PriceInput : SubscriptionEditAction

    /** 이름과 나머지 선택 입력 */
    sealed interface DetailInput : SubscriptionEditAction

    data class NameChanged(
        val name: String,
    ) : DetailInput

    data class PlanSelected(
        val index: Int,
    ) : PriceInput

    data class AmountChanged(
        val input: String,
    ) : PriceInput

    data class CurrencyChanged(
        val currency: Currency,
    ) : PriceInput

    data class CycleOptionSelected(
        val option: CycleOption,
    ) : PriceInput

    data class CustomUnitSelected(
        val unit: CycleUnit,
    ) : PriceInput

    data class CustomIntervalChanged(
        val input: String,
    ) : PriceInput

    data class DateClicked(
        val target: DateTarget,
    ) : SubscriptionEditAction

    data class DatePicked(
        val date: LocalDate,
    ) : SubscriptionEditAction

    data object DatePickerDismissed : SubscriptionEditAction

    data class TrialToggled(
        val enabled: Boolean,
    ) : SubscriptionEditAction

    /** 입력하거나 추천 칩을 눌렀을 때 */
    data class PaymentMethodChanged(
        val input: String,
    ) : DetailInput

    data class CategorySelected(
        val category: Category,
    ) : DetailInput

    data class ReminderSelected(
        val option: ReminderOption,
    ) : DetailInput

    data class ColorSelected(
        val colorKey: String,
    ) : DetailInput

    data class CancelUrlChanged(
        val input: String,
    ) : DetailInput

    data class MemoChanged(
        val input: String,
    ) : DetailInput

    data class TrackUsageToggled(
        val enabled: Boolean,
    ) : DetailInput

    data object SaveClicked : SubscriptionEditAction

    /** 가격 변경 시트에서 적용 시점을 골랐을 때 */
    data class PriceChangeTimingSelected(
        val timing: PriceChangeTiming,
    ) : SubscriptionEditAction

    data object DeleteClicked : SubscriptionEditAction

    data object DeleteConfirmed : SubscriptionEditAction

    data object SheetDismissed : SubscriptionEditAction

    data object CancelClicked : SubscriptionEditAction
}
