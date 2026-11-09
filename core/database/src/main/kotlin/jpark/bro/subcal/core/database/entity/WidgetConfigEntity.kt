package jpark.bro.subcal.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 홈 화면 위젯 설정 (data-model-erd 3장, 위젯은 2차). 출시 후 Migration을 피하려고 v1에 테이블을 둔다.
 * widget_id는 시스템이 주는 appWidgetId라 UUID가 아니다.
 */
@Entity(tableName = "widget_config")
data class WidgetConfigEntity(
    @PrimaryKey @ColumnInfo(name = "widget_id") val widgetId: Int,
    /** SMALL | MEDIUM */
    val size: String,
    /** THEME | DARK | WHITE */
    @ColumnInfo(name = "background_type") val backgroundType: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
