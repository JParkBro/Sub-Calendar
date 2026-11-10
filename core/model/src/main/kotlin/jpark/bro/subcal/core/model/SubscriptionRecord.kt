package jpark.bro.subcal.core.model

/** 결제 · 내 몫을 계산하는 데 필요한 구독 한 건의 저장 값 전부 (data-model-erd 1장) */
data class SubscriptionRecord(
    val subscription: Subscription,
    val priceChanges: List<PriceChange> = emptyList(),
    val pauses: List<Pause> = emptyList(),
    /** 지우지 않은 파티 멤버, [PartyMember.sortOrder]순. 비어 있으면 파티가 아니다 */
    val partyMembers: List<PartyMember> = emptyList(),
)
