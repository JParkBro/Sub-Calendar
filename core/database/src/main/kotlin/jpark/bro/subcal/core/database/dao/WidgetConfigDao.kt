package jpark.bro.subcal.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import jpark.bro.subcal.core.database.entity.WidgetConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetConfigDao {
    @Query("SELECT * FROM widget_config ORDER BY widget_id")
    fun observeAll(): Flow<List<WidgetConfigEntity>>

    @Upsert
    suspend fun upsert(config: WidgetConfigEntity)

    /** 이미 있으면 그대로 둔다 (위젯을 다시 그릴 때마다 설정이 덮이지 않게) */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(config: WidgetConfigEntity)

    @Query("DELETE FROM widget_config WHERE widget_id IN (:widgetIds)")
    suspend fun delete(widgetIds: List<Int>)
}
