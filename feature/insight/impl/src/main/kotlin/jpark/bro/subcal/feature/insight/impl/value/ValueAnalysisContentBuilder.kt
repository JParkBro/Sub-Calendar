package jpark.bro.subcal.feature.insight.impl.value

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.insight.valueAnalysis
import jpark.bro.subcal.core.domain.party.hasParty
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.UsageLog
import java.time.LocalDate

/** 저장된 구독 · 사용 기록에서 가성비 분석 화면 값을 만든다. 계산은 모두 core:domain */
internal fun buildValueAnalysisState(
    records: List<SubscriptionRecord>,
    usageLogs: List<UsageLog>,
    settings: AppSettings,
    today: LocalDate,
): ValueAnalysisState {
    val values = valueAnalysis(records, usageLogs, today, settings.monthStartDay, settings.usdKrwRate)
    val maxCost = values.maxOfOrNull { it.costPerUseKrw ?: 0 } ?: 0
    return ValueAnalysisState(
        isLoading = false,
        month = BillingPeriod.containing(today, settings.monthStartDay).month,
        items =
            values.map { value ->
                val subscription = value.record.subscription
                ValueItem(
                    id = subscription.id,
                    name = subscription.name,
                    colorKey = subscription.colorKey,
                    useCount = value.useCount,
                    costPerUseKrw = value.costPerUseKrw,
                    monthlyKrw = value.monthlyKrw,
                    isParty = value.record.hasParty,
                    barFraction =
                        value.costPerUseKrw?.let { cost -> if (maxCost > 0) cost.toFloat() / maxCost else 0f } ?: 1f,
                )
            },
        considerSuggestions =
            values
                .filter { it.isUnused && !it.record.subscription.considerCancel }
                .map { ConsiderSuggestion(it.record.subscription.id, it.record.subscription.name, it.monthlyKrw) },
    )
}
