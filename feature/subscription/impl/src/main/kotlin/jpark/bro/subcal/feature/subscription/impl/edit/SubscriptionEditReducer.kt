package jpark.bro.subcal.feature.subscription.impl.edit

import jpark.bro.subcal.core.designsystem.component.amountInputToMinor
import jpark.bro.subcal.core.designsystem.component.minorToAmountInput
import jpark.bro.subcal.core.domain.billing.payments
import jpark.bro.subcal.core.domain.billing.priceOn
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Preset
import jpark.bro.subcal.core.model.PresetPlan
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionInput
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.feature.subscription.api.SubscriptionEditKey
import java.time.LocalDate

/** 지난 결제 횟수를 셀 때 쓰는 새 구독의 임시 id */
private const val DRAFT_ID = "draft"

internal fun SubscriptionEditState.reduce(action: SubscriptionEditAction.PriceInput): SubscriptionEditState =
    when (action) {
        is SubscriptionEditAction.PlanSelected -> {
            withPlan(action.index)
        }

        is SubscriptionEditAction.AmountChanged -> {
            copy(amountInput = action.input)
        }

        is SubscriptionEditAction.CurrencyChanged -> {
            copy(currency = action.currency)
        }

        is SubscriptionEditAction.CycleOptionSelected -> {
            copy(cycleOption = action.option)
        }

        is SubscriptionEditAction.CustomUnitSelected -> {
            copy(customUnit = action.unit)
        }

        is SubscriptionEditAction.CustomIntervalChanged -> {
            copy(
                customIntervalInput = action.input.filter(Char::isDigit).take(2),
            )
        }
    }

internal fun SubscriptionEditState.reduce(action: SubscriptionEditAction.DetailInput): SubscriptionEditState =
    when (action) {
        is SubscriptionEditAction.NameChanged -> copy(name = action.name.take(MAX_NAME_LENGTH))
        is SubscriptionEditAction.PaymentMethodChanged -> copy(paymentMethod = action.input)
        is SubscriptionEditAction.CategorySelected -> copy(category = action.category)
        is SubscriptionEditAction.ReminderSelected -> copy(reminder = action.option)
        is SubscriptionEditAction.ColorSelected -> copy(colorKey = action.colorKey)
        is SubscriptionEditAction.CancelUrlChanged -> copy(cancelUrl = action.input)
        is SubscriptionEditAction.MemoChanged -> copy(memo = action.input)
        is SubscriptionEditAction.TrackUsageToggled -> copy(trackUsage = action.enabled)
    }

/** 체험을 켜면 오늘 시작, 종료일(첫 결제일)은 한 달 뒤로 둔다 */
internal fun SubscriptionEditState.withTrial(
    enabled: Boolean,
    today: LocalDate,
): SubscriptionEditState =
    if (enabled) {
        copy(trialStartedOn = today, firstBillingDate = today.plusMonths(1))
    } else {
        copy(trialStartedOn = null)
    }

internal fun SubscriptionEditState.withDate(date: LocalDate): SubscriptionEditState =
    when (datePicker) {
        DateTarget.FIRST_BILLING -> copy(firstBillingDate = date, datePicker = null)
        DateTarget.TRIAL_START -> copy(trialStartedOn = date, datePicker = null)
        null -> this
    }

/** 입력이 바뀔 때마다 다시 계산하는 값: 저장 가능 여부 · 지난 결제 횟수 · 결제 수단 추천 */
internal fun SubscriptionEditState.derived(
    today: LocalDate,
    original: SubscriptionRecord?,
    knownPaymentMethods: List<String>,
): SubscriptionEditState {
    val cycle = billingCycle()
    val amount = amountInputToMinor(amountInput, currency) ?: 0
    val trialValid = trialStartedOn == null || trialStartedOn.isBefore(firstBillingDate)
    val typed = paymentMethod.trim()
    return copy(
        canSave = !isLoading && !isSaving && name.isNotBlank() && amount > 0 && cycle != null && trialValid,
        pastPaymentCount =
            if (cycle != null && firstBillingDate.isBefore(today)) pastPayments(cycle, today, original) else 0,
        paymentMethodSuggestions = knownPaymentMethods.filter { it != typed && it.contains(typed, ignoreCase = true) },
    )
}

/** 저장할 값. 저장할 수 없는 상태면 null */
internal fun SubscriptionEditState.toInput(original: SubscriptionRecord?): SubscriptionInput? {
    val cycle = billingCycle()
    val amount = amountInputToMinor(amountInput, currency)
    if (!canSave || cycle == null || amount == null) return null
    return SubscriptionInput(
        presetId = presetId,
        name = name.trim(),
        // 프리셋 요금제가 없으면 저장된 요금제 이름을 그대로 둔다
        planName = if (plans.isEmpty()) original?.subscription?.planName else selectedPlanIndex?.let { plans[it].name },
        category = category,
        amount = Money(amount, currency),
        cycle = cycle,
        firstBillingDate = firstBillingDate,
        trialStartedOn = trialStartedOn,
        paymentMethod = paymentMethod.trim().ifEmpty { null },
        reminderMode = reminder.mode,
        reminderDaysBefore = reminder.daysBefore,
        colorKey = colorKey,
        cancelUrl = cancelUrl.trim().ifEmpty { null },
        memo = memo.trim().ifEmpty { null },
        trackUsage = trackUsage,
    )
}

internal fun SubscriptionEditState.withPlan(index: Int): SubscriptionEditState {
    val plan = plans[index]
    return copy(
        selectedPlanIndex = index,
        amountInput = minorToAmountInput(plan.amount),
        currency = plan.amount.currency,
    ).withCycle(plan.cycle)
}

internal fun SubscriptionEditState.withCycle(cycle: BillingCycle): SubscriptionEditState {
    val option = cycle.toOption()
    return if (option == CycleOption.CUSTOM) {
        copy(cycleOption = option, customUnit = cycle.unit, customIntervalInput = cycle.interval.toString())
    } else {
        copy(cycleOption = option)
    }
}

/** 첫 결제일부터 오늘까지(오늘 포함) 생긴 결제 횟수. 수정이면 저장된 일시정지 · 해지를 반영한다 */
private fun SubscriptionEditState.pastPayments(
    cycle: BillingCycle,
    today: LocalDate,
    original: SubscriptionRecord?,
): Int {
    val base =
        original ?: SubscriptionRecord(
            Subscription(
                id = DRAFT_ID,
                name = name,
                amount = Money(0, currency),
                cycle = cycle,
                firstBillingDate = firstBillingDate,
                colorKey = colorKey,
                createdAt = 0,
                updatedAt = 0,
            ),
        )
    val draft = base.copy(subscription = base.subscription.copy(cycle = cycle, firstBillingDate = firstBillingDate))
    return draft.payments(firstBillingDate, today.plusDays(1)).size
}
