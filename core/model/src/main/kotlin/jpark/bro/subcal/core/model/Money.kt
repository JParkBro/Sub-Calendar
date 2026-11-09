package jpark.bro.subcal.core.model

/** 금액. [amountMinor]는 최소 단위: KRW는 원, USD는 센트 (product-spec 2-4) */
data class Money(
    val amountMinor: Long,
    val currency: Currency,
)

enum class Currency {
    KRW,
    USD,
}
