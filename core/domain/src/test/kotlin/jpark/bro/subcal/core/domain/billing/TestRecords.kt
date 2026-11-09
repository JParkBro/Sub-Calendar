package jpark.bro.subcal.core.domain.billing

import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Pause
import jpark.bro.subcal.core.model.PriceChange
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.model.TrialOutcome
import java.time.LocalDate

internal fun date(text: String): LocalDate = LocalDate.parse(text)

internal fun subscription(
    name: String = "테스트",
    amount: Long = 10_000,
    unit: CycleUnit = CycleUnit.MONTH,
    interval: Int = 1,
    first: String = "2026-01-01",
): Subscription =
    Subscription(
        id = name,
        name = name,
        amount = Money(amount, Currency.KRW),
        cycle = BillingCycle(unit, interval),
        firstBillingDate = date(first),
        colorKey = "BLUE",
        createdAt = 0,
        updatedAt = 0,
    )

internal fun Subscription.usd(): Subscription = copy(amount = amount.copy(currency = Currency.USD))

internal fun Subscription.trial(
    startedOn: String,
    outcome: TrialOutcome,
): Subscription = copy(trialStartedOn = date(startedOn), trialOutcome = outcome)

internal fun Subscription.cancelled(on: String): Subscription =
    copy(status = SubscriptionStatus.CANCELLED, cancelledOn = date(on))

internal fun Subscription.paused(): Subscription = copy(status = SubscriptionStatus.PAUSED)

internal fun priceChange(
    effectiveFrom: String,
    amount: Long,
): PriceChange = PriceChange(effectiveFrom, "테스트", date(effectiveFrom), amount, createdAt = 0)

internal fun pause(
    start: String,
    end: String? = null,
): Pause = Pause(start, "테스트", date(start), end?.let(::date), createdAt = 0)

internal fun Subscription.record(
    priceChanges: List<PriceChange> = emptyList(),
    pauses: List<Pause> = emptyList(),
): SubscriptionRecord = SubscriptionRecord(this, priceChanges, pauses)

internal fun SubscriptionRecord.dates(
    from: String,
    untilExclusive: String,
): List<String> = payments(date(from), date(untilExclusive)).map { it.date.toString() }
