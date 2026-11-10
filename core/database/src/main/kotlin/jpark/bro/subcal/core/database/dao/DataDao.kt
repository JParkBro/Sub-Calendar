package jpark.bro.subcal.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import jpark.bro.subcal.core.database.entity.PauseEntity
import jpark.bro.subcal.core.database.entity.PriceChangeEntity
import jpark.bro.subcal.core.database.entity.SubscriptionEntity

/** 구독 데이터 전체를 다루는 작업: 백업 불러오기 · 모든 데이터 삭제 · 지운 행 정리 (data-model-erd 6장) */
@Dao
interface DataDao {
    @Query("DELETE FROM subscription")
    suspend fun deleteAllSubscriptions()

    @Insert
    suspend fun insertSubscriptions(subscriptions: List<SubscriptionEntity>)

    @Insert
    suspend fun insertPriceChanges(priceChanges: List<PriceChangeEntity>)

    @Insert
    suspend fun insertPauses(pauses: List<PauseEntity>)

    /**
     * 백업 불러오기 (data-model-erd 6장): 구독 · 가격 변경 · 일시정지를 모두 지우고(삭제한 행 포함) 넣는다.
     * 하나라도 실패하면 트랜잭션이 되돌려 아무것도 바뀌지 않는다.
     */
    @Transaction
    suspend fun replaceAll(
        subscriptions: List<SubscriptionEntity>,
        priceChanges: List<PriceChangeEntity>,
        pauses: List<PauseEntity>,
    ) {
        // 하위 행은 외래키 CASCADE로 함께 지워진다
        deleteAllSubscriptions()
        insertSubscriptions(subscriptions)
        insertPriceChanges(priceChanges)
        insertPauses(pauses)
    }

    @Query("DELETE FROM widget_config")
    suspend fun deleteAllWidgetConfigs()

    /** 모든 데이터 삭제 (product-spec 6장): 구독(하위 행 CASCADE)과 위젯 설정까지 지운다 */
    @Transaction
    suspend fun deleteAll() {
        deleteAllSubscriptions()
        deleteAllWidgetConfigs()
    }

    /** soft delete한 지 [cutoff] 이전인 구독을 지운다 (하위 행 CASCADE). 지운 행 수 */
    @Query("DELETE FROM subscription WHERE deleted_at IS NOT NULL AND deleted_at < :cutoff")
    suspend fun purgeDeletedBefore(cutoff: Long): Int
}
