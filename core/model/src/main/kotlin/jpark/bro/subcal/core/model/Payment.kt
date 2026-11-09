package jpark.bro.subcal.core.model

import java.time.LocalDate

/** 규칙에서 계산한 결제 한 건. 저장하지 않는다 (data-model-erd 1장) */
data class Payment(
    val subscription: Subscription,
    val date: LocalDate,
    val amount: Money,
)
