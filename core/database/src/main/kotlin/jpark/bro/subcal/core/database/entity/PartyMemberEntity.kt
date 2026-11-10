package jpark.bro.subcal.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** 파티 멤버 (data-model-erd 2장, 3차). "나"는 행으로 저장하지 않는다 */
@Entity(
    tableName = "party_member",
    foreignKeys = [
        ForeignKey(
            entity = SubscriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["subscription_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("subscription_id")],
)
data class PartyMemberEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "subscription_id") val subscriptionId: String,
    val name: String,
    /** 분담 방식이 CUSTOM일 때만 */
    @ColumnInfo(name = "share_amount_minor") val shareAmountMinor: Long?,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
