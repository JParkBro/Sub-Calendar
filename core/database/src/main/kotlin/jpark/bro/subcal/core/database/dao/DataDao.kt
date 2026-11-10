package jpark.bro.subcal.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import jpark.bro.subcal.core.database.entity.PartyMemberEntity
import jpark.bro.subcal.core.database.entity.PartySettlementEntity
import jpark.bro.subcal.core.database.entity.PauseEntity
import jpark.bro.subcal.core.database.entity.PriceChangeEntity
import jpark.bro.subcal.core.database.entity.SubscriptionEntity
import jpark.bro.subcal.core.database.entity.UsageLogEntity

/** 구독 데이터 전체를 다루는 작업: 백업 불러오기 · 모든 데이터 삭제 · 지운 행 정리 (data-model-erd 6장) */
@Dao
interface DataDao {
    @Query("DELETE FROM subscription")
    suspend fun deleteAllSubscriptions()

    /** 구독과 하위 행을 넣는다. 부모 행이 먼저 들어가도록 매개변수 순서대로 넣는다 */
    @Suppress("LongParameterList")
    @Insert
    suspend fun insertAll(
        subscriptions: List<SubscriptionEntity>,
        priceChanges: List<PriceChangeEntity>,
        pauses: List<PauseEntity>,
        usageLogs: List<UsageLogEntity>,
        partyMembers: List<PartyMemberEntity>,
        partySettlements: List<PartySettlementEntity>,
    )

    /** 백업 내보내기: 삭제되지 않은 구독의 삭제되지 않은 사용 기록 */
    @Query(
        """
        SELECT usage_log.* FROM usage_log
        JOIN subscription ON subscription.id = usage_log.subscription_id
        WHERE usage_log.deleted_at IS NULL AND subscription.deleted_at IS NULL
        ORDER BY usage_log.used_on, usage_log.created_at
        """,
    )
    suspend fun getUsageLogs(): List<UsageLogEntity>

    /** 백업 내보내기: 삭제되지 않은 구독의 삭제되지 않은 파티 멤버 */
    @Query(
        """
        SELECT party_member.* FROM party_member
        JOIN subscription ON subscription.id = party_member.subscription_id
        WHERE party_member.deleted_at IS NULL AND subscription.deleted_at IS NULL
        ORDER BY party_member.subscription_id, party_member.sort_order
        """,
    )
    suspend fun getPartyMembers(): List<PartyMemberEntity>

    /** 백업 내보내기: [getPartyMembers]에 나오는 멤버의 정산 */
    @Query(
        """
        SELECT party_settlement.* FROM party_settlement
        JOIN party_member ON party_member.id = party_settlement.party_member_id
        JOIN subscription ON subscription.id = party_member.subscription_id
        WHERE party_member.deleted_at IS NULL AND subscription.deleted_at IS NULL
        ORDER BY party_settlement.party_member_id, party_settlement.period
        """,
    )
    suspend fun getPartySettlements(): List<PartySettlementEntity>

    /**
     * 백업 불러오기 (data-model-erd 6장): 구독과 하위 행을 모두 지우고(삭제한 행 포함) 넣는다.
     * 하나라도 실패하면 트랜잭션이 되돌려 아무것도 바뀌지 않는다.
     */
    @Suppress("LongParameterList")
    @Transaction
    suspend fun replaceAll(
        subscriptions: List<SubscriptionEntity>,
        priceChanges: List<PriceChangeEntity>,
        pauses: List<PauseEntity>,
        usageLogs: List<UsageLogEntity>,
        partyMembers: List<PartyMemberEntity>,
        partySettlements: List<PartySettlementEntity>,
    ) {
        // 하위 행은 외래키 CASCADE로 함께 지워진다
        deleteAllSubscriptions()
        insertAll(subscriptions, priceChanges, pauses, usageLogs, partyMembers, partySettlements)
    }

    @Query("DELETE FROM widget_config")
    suspend fun deleteAllWidgetConfigs()

    /** 모든 데이터 삭제 (product-spec 6장): 구독(하위 행 CASCADE)과 위젯 설정까지 지운다 */
    @Transaction
    suspend fun deleteAll() {
        deleteAllSubscriptions()
        deleteAllWidgetConfigs()
    }

    @Query("DELETE FROM subscription WHERE deleted_at IS NOT NULL AND deleted_at < :cutoff")
    suspend fun purgeSubscriptionsDeletedBefore(cutoff: Long): Int

    @Query("DELETE FROM usage_log WHERE deleted_at IS NOT NULL AND deleted_at < :cutoff")
    suspend fun purgeUsageLogsDeletedBefore(cutoff: Long): Int

    @Query("DELETE FROM party_member WHERE deleted_at IS NOT NULL AND deleted_at < :cutoff")
    suspend fun purgePartyMembersDeletedBefore(cutoff: Long): Int

    /** soft delete한 지 [cutoff] 이전인 구독 · 사용 기록 · 파티 멤버를 지운다 (하위 행 CASCADE). 지운 행 수 */
    @Transaction
    suspend fun purgeDeletedBefore(cutoff: Long): Int =
        purgeSubscriptionsDeletedBefore(cutoff) +
            purgeUsageLogsDeletedBefore(cutoff) +
            purgePartyMembersDeletedBefore(cutoff)
}
