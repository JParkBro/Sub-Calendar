package jpark.bro.subcal.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import jpark.bro.subcal.core.database.dao.DataDao
import jpark.bro.subcal.core.database.dao.SubscriptionDao
import jpark.bro.subcal.core.database.dao.UsageLogDao
import jpark.bro.subcal.core.database.dao.WidgetConfigDao
import jpark.bro.subcal.core.database.entity.PartyMemberEntity
import jpark.bro.subcal.core.database.entity.PartySettlementEntity
import jpark.bro.subcal.core.database.entity.PauseEntity
import jpark.bro.subcal.core.database.entity.PriceChangeEntity
import jpark.bro.subcal.core.database.entity.SubscriptionEntity
import jpark.bro.subcal.core.database.entity.UsageLogEntity
import jpark.bro.subcal.core.database.entity.WidgetConfigEntity

/**
 * 출시 전까지 version 1을 고쳐 쓴다 (docs/subcal/README.md "공통 문서와 다르게 가는 부분").
 * 출시 후에는 version을 올리고 Migration + 마이그레이션 테스트를 추가한다.
 */
@Database(
    entities = [
        SubscriptionEntity::class,
        PriceChangeEntity::class,
        PauseEntity::class,
        WidgetConfigEntity::class,
        UsageLogEntity::class,
        PartyMemberEntity::class,
        PartySettlementEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class SubCalDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao

    abstract fun dataDao(): DataDao

    abstract fun widgetConfigDao(): WidgetConfigDao

    abstract fun usageLogDao(): UsageLogDao
}
