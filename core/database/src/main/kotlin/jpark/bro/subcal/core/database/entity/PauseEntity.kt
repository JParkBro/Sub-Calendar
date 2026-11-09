package jpark.bro.subcal.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** 일시정지 기간 (data-model-erd 2장). end_date가 null이면 일시정지 중. 겹침은 Repository에서 검사한다 */
@Entity(
    tableName = "pause",
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
data class PauseEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "subscription_id") val subscriptionId: String,
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "end_date") val endDate: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
