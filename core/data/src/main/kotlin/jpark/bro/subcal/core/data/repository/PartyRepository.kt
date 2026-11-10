package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.model.PartySettlement
import jpark.bro.subcal.core.model.PartySplitMode
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/** 파티 멤버 · 분담 · 정산 (product-spec 4-9). 멤버 목록은 [SubscriptionRepository]의 구독에 들어 있다 */
interface PartyRepository {
    /** 지우지 않은 구독 · 멤버의 "받음" 체크 */
    fun observeSettlements(): Flow<List<PartySettlement>>

    /** 이름은 앞뒤 공백을 빼고 1~10자, 멤버는 9명까지. 새 멤버의 id를 돌려준다 */
    suspend fun addMember(
        subscriptionId: String,
        name: String,
    ): Result<String>

    suspend fun renameMember(
        memberId: String,
        name: String,
    ): Result<Unit>

    /** soft delete. 그 멤버의 정산도 보이지 않는다 */
    suspend fun removeMember(memberId: String): Result<Unit>

    /**
     * 분담 방식. [PartySplitMode.CUSTOM]이면 [shares]에 지금 멤버 전원의 금액(최소 단위, 0 이상)이 있어야 하고
     * 합이 오늘 금액 이하여야 한다. 균등이면 [shares]는 무시하고 저장한 금액을 지운다.
     */
    suspend fun setSplit(
        subscriptionId: String,
        mode: PartySplitMode,
        shares: Map<String, Long> = emptyMap(),
    ): Result<Unit>

    /** [period] 정산 "받음" 체크 · 해제 (해제하면 행을 지운다) */
    suspend fun setSettled(
        memberId: String,
        period: YearMonth,
        settled: Boolean,
    ): Result<Unit>
}
