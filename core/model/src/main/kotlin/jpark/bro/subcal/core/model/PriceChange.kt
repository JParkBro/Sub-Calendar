package jpark.bro.subcal.core.model

import java.time.LocalDate

/** [effectiveFrom]부터 적용되는 새 금액. 통화는 구독의 통화를 따른다 (product-spec 2-5) */
data class PriceChange(
    val id: String,
    val subscriptionId: String,
    val effectiveFrom: LocalDate,
    val amountMinor: Long,
    /** epoch millis */
    val createdAt: Long,
)
