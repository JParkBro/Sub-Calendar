package jpark.bro.subcal.core.model

import java.time.YearMonth

/** 파티 멤버 (product-spec 4-9). "나"는 저장하지 않으므로 인원 = 멤버 수 + 1 */
data class PartyMember(
    val id: String,
    val subscriptionId: String,
    /** 1~10자 */
    val name: String,
    /** 분담 방식이 [PartySplitMode.CUSTOM]일 때만 */
    val shareAmountMinor: Long? = null,
    val sortOrder: Int,
    /** epoch millis */
    val createdAt: Long,
    /** epoch millis */
    val updatedAt: Long,
)

/** 멤버가 [period] 몫을 보냈다는 "받음" 체크 (product-spec 4-9) */
data class PartySettlement(
    val partyMemberId: String,
    /** 이번 달 기간 이름 */
    val period: YearMonth,
    /** epoch millis */
    val settledAt: Long,
)
