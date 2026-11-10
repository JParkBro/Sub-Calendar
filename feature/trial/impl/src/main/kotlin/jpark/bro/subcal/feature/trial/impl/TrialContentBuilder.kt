package jpark.bro.subcal.feature.trial.impl

import jpark.bro.subcal.core.domain.billing.priceOn
import jpark.bro.subcal.core.domain.insight.savedKrw
import jpark.bro.subcal.core.domain.trial.activeTrials
import jpark.bro.subcal.core.domain.trial.endedTrials
import jpark.bro.subcal.core.domain.trial.trialEndedOn
import jpark.bro.subcal.core.domain.trial.trialProgress
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 저장된 구독에서 무료체험 화면 값을 만든다. 계산은 core:domain */
internal fun buildTrialState(
    records: List<SubscriptionRecord>,
    usdKrwRate: Long,
    today: LocalDate,
): TrialState =
    TrialState(
        isLoading = false,
        active =
            activeTrials(records, today).map { record ->
                val subscription = record.subscription
                val start = subscription.trialStartedOn ?: subscription.firstBillingDate
                ActiveTrial(
                    id = subscription.id,
                    name = subscription.name,
                    colorKey = subscription.colorKey,
                    firstBillingDate = subscription.firstBillingDate,
                    amount = record.priceOn(subscription.firstBillingDate),
                    daysUntil = ChronoUnit.DAYS.between(today, subscription.firstBillingDate).toInt(),
                    progress = record.trialProgress(today),
                    totalDays = ChronoUnit.DAYS.between(start, subscription.firstBillingDate).toInt(),
                    elapsedDays =
                        ChronoUnit.DAYS
                            .between(start, today)
                            .toInt()
                            .coerceAtLeast(0),
                )
            },
        ended =
            endedTrials(records, today).map { record ->
                val subscription = record.subscription
                EndedTrial(
                    id = subscription.id,
                    name = subscription.name,
                    colorKey = subscription.colorKey,
                    endedOn = record.trialEndedOn(),
                    cancelled = subscription.status == SubscriptionStatus.CANCELLED,
                    savedKrw = record.savedKrw(today, usdKrwRate),
                )
            },
    )
