package jpark.bro.subcal.core.data.mapper

import jpark.bro.subcal.core.database.entity.PauseEntity
import jpark.bro.subcal.core.database.entity.PriceChangeEntity
import jpark.bro.subcal.core.database.entity.SubscriptionEntity
import jpark.bro.subcal.core.database.entity.SubscriptionWithRules
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.PartySplitMode
import jpark.bro.subcal.core.model.Pause
import jpark.bro.subcal.core.model.PriceChange
import jpark.bro.subcal.core.model.ReminderMode
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.model.TrialOutcome
import java.time.LocalDate

internal fun SubscriptionWithRules.toModel(): SubscriptionRecord =
    SubscriptionRecord(
        subscription = subscription.toModel(),
        priceChanges = priceChanges.map(PriceChangeEntity::toModel),
        pauses = pauses.map(PauseEntity::toModel),
    )

internal fun SubscriptionEntity.toModel(): Subscription =
    Subscription(
        id = id,
        presetId = presetId,
        name = name,
        planName = planName,
        category = Category.valueOf(category),
        amount = Money(amountMinor, Currency.valueOf(currency)),
        cycle = BillingCycle(CycleUnit.valueOf(cycleUnit), cycleInterval),
        firstBillingDate = LocalDate.parse(firstBillingDate),
        trialStartedOn = trialStartedOn?.let(LocalDate::parse),
        trialOutcome = TrialOutcome.valueOf(trialOutcome),
        status = SubscriptionStatus.valueOf(status),
        cancelledOn = cancelledOn?.let(LocalDate::parse),
        paymentMethod = paymentMethod,
        reminderMode = ReminderMode.valueOf(reminderMode),
        reminderDaysBefore = reminderDaysBefore,
        colorKey = colorKey,
        cancelUrl = cancelUrl,
        memo = memo,
        considerCancel = considerCancel,
        trackUsage = trackUsage,
        partySplitMode = PartySplitMode.valueOf(partySplitMode),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/** 모델에는 삭제된 구독이 없으므로 deleted_at은 항상 null이다 */
internal fun Subscription.toEntity(): SubscriptionEntity =
    SubscriptionEntity(
        id = id,
        presetId = presetId,
        name = name,
        planName = planName,
        category = category.name,
        amountMinor = amount.amountMinor,
        currency = amount.currency.name,
        cycleUnit = cycle.unit.name,
        cycleInterval = cycle.interval,
        firstBillingDate = firstBillingDate.toString(),
        trialStartedOn = trialStartedOn?.toString(),
        trialOutcome = trialOutcome.name,
        status = status.name,
        cancelledOn = cancelledOn?.toString(),
        paymentMethod = paymentMethod,
        reminderMode = reminderMode.name,
        reminderDaysBefore = reminderDaysBefore,
        colorKey = colorKey,
        cancelUrl = cancelUrl,
        memo = memo,
        considerCancel = considerCancel,
        trackUsage = trackUsage,
        partySplitMode = partySplitMode.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = null,
    )

internal fun PriceChangeEntity.toModel(): PriceChange =
    PriceChange(id, subscriptionId, LocalDate.parse(effectiveFrom), amountMinor, createdAt)

internal fun PriceChange.toEntity(): PriceChangeEntity =
    PriceChangeEntity(id, subscriptionId, effectiveFrom.toString(), amountMinor, createdAt)

internal fun PauseEntity.toModel(): Pause =
    Pause(id, subscriptionId, LocalDate.parse(startDate), endDate?.let(LocalDate::parse), createdAt)

internal fun Pause.toEntity(): PauseEntity =
    PauseEntity(id, subscriptionId, startDate.toString(), endDate?.toString(), createdAt)
