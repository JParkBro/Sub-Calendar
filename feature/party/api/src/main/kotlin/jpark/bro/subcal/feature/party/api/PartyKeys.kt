package jpark.bro.subcal.feature.party.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 파티 정산 (product-spec 4-9): 구독 상세 · 파티 관리 · 정산 알림에서 연다 */
@Serializable
data class PartyKey(
    val subscriptionId: String,
) : NavKey

/** 더보기 → 파티 관리: 파티가 있는 구독 목록 (product-spec 6장) */
@Serializable
data object PartyListKey : NavKey
