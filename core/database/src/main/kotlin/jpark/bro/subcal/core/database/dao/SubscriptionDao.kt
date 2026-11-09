package jpark.bro.subcal.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import jpark.bro.subcal.core.database.entity.PauseEntity
import jpark.bro.subcal.core.database.entity.PriceChangeEntity
import jpark.bro.subcal.core.database.entity.SubscriptionEntity
import jpark.bro.subcal.core.database.entity.SubscriptionWithRules
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    /** 삭제되지 않은 구독, 등록순 */
    @Transaction
    @Query("SELECT * FROM subscription WHERE deleted_at IS NULL ORDER BY created_at, id")
    fun observeAll(): Flow<List<SubscriptionWithRules>>

    /** 삭제됐거나 없으면 null */
    @Transaction
    @Query("SELECT * FROM subscription WHERE id = :id AND deleted_at IS NULL")
    fun observe(id: String): Flow<SubscriptionWithRules?>

    /** 삭제됐거나 없으면 null */
    @Transaction
    @Query("SELECT * FROM subscription WHERE id = :id AND deleted_at IS NULL")
    suspend fun get(id: String): SubscriptionWithRules?

    @Upsert
    suspend fun upsertSubscription(subscription: SubscriptionEntity)

    @Upsert
    suspend fun upsertPriceChange(priceChange: PriceChangeEntity)

    @Query("DELETE FROM price_change WHERE subscription_id = :subscriptionId")
    suspend fun deletePriceChanges(subscriptionId: String)

    @Upsert
    suspend fun upsertPause(pause: PauseEntity)

    @Query("DELETE FROM pause WHERE id = :id")
    suspend fun deletePause(id: String)

    @Query("UPDATE subscription SET deleted_at = :deletedAt, updated_at = :deletedAt WHERE id = :id")
    suspend fun softDelete(
        id: String,
        deletedAt: Long,
    )

    /**
     * 구독과 하위 행 변경을 한 트랜잭션으로 저장한다.
     * [clearPriceChanges]면 기존 가격 변경을 지운 뒤 [priceChange]를 넣는다 (product-spec 2-5, 2-6)
     */
    @Transaction
    suspend fun save(
        subscription: SubscriptionEntity,
        priceChange: PriceChangeEntity? = null,
        clearPriceChanges: Boolean = false,
        pauseToSave: PauseEntity? = null,
        pauseIdToDelete: String? = null,
    ) {
        upsertSubscription(subscription)
        if (clearPriceChanges) deletePriceChanges(subscription.id)
        priceChange?.let { upsertPriceChange(it) }
        pauseToSave?.let { upsertPause(it) }
        pauseIdToDelete?.let { deletePause(it) }
    }
}
