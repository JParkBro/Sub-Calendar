package jpark.bro.subcal.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import jpark.bro.subcal.core.database.entity.UsageLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageLogDao {
    /** 삭제되지 않은 구독의 삭제되지 않은 사용 기록, 날짜순 */
    @Query(
        """
        SELECT usage_log.* FROM usage_log
        JOIN subscription ON subscription.id = usage_log.subscription_id
        WHERE usage_log.deleted_at IS NULL AND subscription.deleted_at IS NULL
        ORDER BY usage_log.used_on, usage_log.created_at
        """,
    )
    fun observeAll(): Flow<List<UsageLogEntity>>

    @Insert
    suspend fun insert(usageLog: UsageLogEntity)

    /** 바뀐 행 수. 없거나 이미 지웠으면 0 */
    @Query("UPDATE usage_log SET deleted_at = :deletedAt WHERE id = :id AND deleted_at IS NULL")
    suspend fun softDelete(
        id: String,
        deletedAt: Long,
    ): Int
}
