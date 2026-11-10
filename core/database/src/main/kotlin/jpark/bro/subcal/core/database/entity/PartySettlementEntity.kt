package jpark.bro.subcal.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

/** 멤버의 달별 "받음" 체크 (data-model-erd 2장, 3차). 체크를 풀면 행을 지운다 (soft delete 예외) */
@Entity(
    tableName = "party_settlement",
    primaryKeys = ["party_member_id", "period"],
    foreignKeys = [
        ForeignKey(
            entity = PartyMemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["party_member_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PartySettlementEntity(
    @ColumnInfo(name = "party_member_id") val partyMemberId: String,
    /** yyyy-MM, 이번 달 기간 이름 */
    val period: String,
    @ColumnInfo(name = "settled_at") val settledAt: Long,
)
