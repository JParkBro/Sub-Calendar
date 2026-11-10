package jpark.bro.subcal.core.notification

import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionRecord
import java.time.LocalDate

/** 매월 [first]의 "일"에 결제하는 구독 */
internal fun monthly(
    name: String,
    first: LocalDate,
    amount: Long = 10_000,
): SubscriptionRecord =
    SubscriptionRecord(
        Subscription(
            id = name,
            name = name,
            amount = Money(amount, Currency.KRW),
            cycle = BillingCycle(CycleUnit.MONTH),
            firstBillingDate = first,
            colorKey = "BLUE",
            createdAt = 0,
            updatedAt = 0,
        ),
    )
