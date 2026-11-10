package jpark.bro.subcal.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import jpark.bro.subcal.core.database.entity.PauseEntity
import jpark.bro.subcal.core.database.entity.PriceChangeEntity
import jpark.bro.subcal.core.database.entity.SubscriptionEntity

/** 백업 불러오기 전용: 구독 데이터를 통째로 바꾼다 (data-model-erd 6장) */
@Dao
interface BackupDao {
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
}
