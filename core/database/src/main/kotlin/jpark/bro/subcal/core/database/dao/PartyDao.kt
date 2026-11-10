package jpark.bro.subcal.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import jpark.bro.subcal.core.database.entity.PartyMemberEntity
import jpark.bro.subcal.core.database.entity.PartySettlementEntity
import kotlinx.coroutines.flow.Flow

/** 파티 멤버 · 정산 (product-spec 4-9). 멤버 목록은 구독과 함께 `SubscriptionDao`로 읽는다 */
@Dao
interface PartyDao {
    /** 지우지 않은 구독 · 멤버의 정산 */
    @Query(
        """
        SELECT party_settlement.* FROM party_settlement
        JOIN party_member ON party_member.id = party_settlement.party_member_id
        JOIN subscription ON subscription.id = party_member.subscription_id
        WHERE party_member.deleted_at IS NULL AND subscription.deleted_at IS NULL
        """,
    )
    fun observeSettlements(): Flow<List<PartySettlementEntity>>

    /** 지우지 않은 멤버. 없거나 지웠으면 null */
    @Query("SELECT * FROM party_member WHERE id = :id AND deleted_at IS NULL")
    suspend fun getMember(id: String): PartyMemberEntity?

    @Upsert
    suspend fun upsertMembers(members: List<PartyMemberEntity>)

    @Query("UPDATE party_member SET deleted_at = :deletedAt, updated_at = :deletedAt WHERE id = :id")
    suspend fun softDeleteMember(
        id: String,
        deletedAt: Long,
    )

    @Query("UPDATE subscription SET party_split_mode = :mode, updated_at = :updatedAt WHERE id = :subscriptionId")
    suspend fun updateSplitMode(
        subscriptionId: String,
        mode: String,
        updatedAt: Long,
    )

    /** 분담 방식과 멤버별 금액을 함께 바꾼다 */
    @Transaction
    suspend fun saveSplit(
        subscriptionId: String,
        mode: String,
        members: List<PartyMemberEntity>,
        updatedAt: Long,
    ) {
        updateSplitMode(subscriptionId, mode, updatedAt)
        upsertMembers(members)
    }

    @Upsert
    suspend fun upsertSettlement(settlement: PartySettlementEntity)

    @Query("DELETE FROM party_settlement WHERE party_member_id = :memberId AND period = :period")
    suspend fun deleteSettlement(
        memberId: String,
        period: String,
    )
}
