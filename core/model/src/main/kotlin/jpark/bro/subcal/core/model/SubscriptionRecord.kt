package jpark.bro.subcal.core.model

/** 결제를 계산하는 데 필요한 구독 한 건의 저장 값 전부 (data-model-erd 1장) */
data class SubscriptionRecord(
    val subscription: Subscription,
    val priceChanges: List<PriceChange> = emptyList(),
    val pauses: List<Pause> = emptyList(),
)
