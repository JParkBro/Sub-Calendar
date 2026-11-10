package jpark.bro.subcal.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** 사용 기록 (data-model-erd 2장, 3차). 하루에 여러 번 쓸 수 있다. 실행 취소는 soft delete */
@Entity(
    tableName = "usage_log",
    foreignKeys = [
        ForeignKey(
            entity = SubscriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["subscription_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["subscription_id", "used_on"])],
)
data class UsageLogEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "subscription_id") val subscriptionId: String,
    @ColumnInfo(name = "used_on") val usedOn: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
