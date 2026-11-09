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

/** 새 구독: 프리셋이 있으면 이름 · 카테고리 · 색 · 해지 주소와 요금제를 채운다 */
internal fun SubscriptionEditState.newSubscription(
    key: SubscriptionEditKey,
    preset: Preset?,
    pricesAsOf: String?,
    defaultPaymentMethod: String?,
    today: LocalDate,
): SubscriptionEditState {
    val filled =
        copy(
            isLoading = false,
            presetId = preset?.id,
            plans = preset?.plans.orEmpty(),
            pricesAsOf = preset?.let { pricesAsOf },
            name = preset?.name.orEmpty(),
            category = preset?.category ?: Category.ETC,
            colorKey = preset?.colorKey ?: DEFAULT_COLOR_KEY,
            cancelUrl = preset?.cancelUrl.orEmpty(),
            firstBillingDate = today,
            paymentMethod = defaultPaymentMethod.orEmpty(),
        )
    // 첫 요금제(또는 키로 받은 요금제)를 골라 바로 저장할 수 있게 한다
    val planIndex = (key.planIndex ?: 0).takeIf { it in filled.plans.indices }
    val withPlan = if (planIndex != null) filled.withPlan(planIndex) else filled
    return if (key.startAsTrial) withPlan.withTrial(enabled = true, today = today) else withPlan
}

/** 수정: 저장된 값. 금액은 가격 변경을 반영한 오늘 유효한 금액이다 (2-5) */
internal fun SubscriptionEditState.fromRecord(
    record: SubscriptionRecord,
    plans: List<PresetPlan>,
    today: LocalDate,
): SubscriptionEditState {
    val subscription = record.subscription
    return copy(
        isLoading = false,
        presetId = subscription.presetId,
        plans = plans,
        selectedPlanIndex = plans.indexOfFirst { it.name == subscription.planName }.takeIf { it >= 0 },
        name = subscription.name,
        amountInput = minorToAmountInput(record.priceOn(today)),
        currency = subscription.amount.currency,
        firstBillingDate = subscription.firstBillingDate,
        trialStartedOn = subscription.trialStartedOn,
        paymentMethod = subscription.paymentMethod.orEmpty(),
        category = subscription.category,
        reminder = reminderOptionOf(subscription.reminderMode, subscription.reminderDaysBefore),
        colorKey = subscription.colorKey,
        cancelUrl = subscription.cancelUrl.orEmpty(),
        memo = subscription.memo.orEmpty(),
    ).withCycle(subscription.cycle)
}
