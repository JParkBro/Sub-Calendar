package jpark.bro.subcal.feature.party.impl.list

import jpark.bro.subcal.core.model.Money

/** 더보기 → 파티 관리: 파티가 있는 구독 (product-spec 6장), 이름순. 해지한 구독도 정산이 남아 있을 수 있어 보여준다 */
internal data class PartyListState(
    val isLoading: Boolean = true,
    val items: List<PartyListItem> = emptyList(),
)

internal data class PartyListItem(
    val subscriptionId: String,
    val name: String,
    val colorKey: String,
    /** 나 포함 */
    val partySize: Int,
    val myShare: Money,
    /** 가장 최근 정산 기간의 미정산 멤버 수 */
    val unsettledCount: Int,
)
