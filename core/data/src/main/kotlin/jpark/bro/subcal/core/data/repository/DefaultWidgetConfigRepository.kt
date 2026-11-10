package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.common.suspendRunCatching
import jpark.bro.subcal.core.database.dao.WidgetConfigDao
import jpark.bro.subcal.core.database.entity.WidgetConfigEntity
import jpark.bro.subcal.core.model.WidgetBackground
import jpark.bro.subcal.core.model.WidgetConfig
import jpark.bro.subcal.core.model.WidgetSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock

internal class DefaultWidgetConfigRepository(
    private val dao: WidgetConfigDao,
    private val clock: Clock,
) : WidgetConfigRepository {
    override fun observeConfigs(): Flow<List<WidgetConfig>> =
        dao.observeAll().map { rows ->
            rows.mapNotNull { it.toModel() }
        }

    override suspend fun saveConfigIfAbsent(config: WidgetConfig): Result<Unit> =
        suspendRunCatching { dao.insertIfAbsent(config.toEntity(clock.millis())) }

    override suspend fun saveConfig(config: WidgetConfig): Result<Unit> =
        suspendRunCatching {
            dao.upsert(config.toEntity(clock.millis()))
        }

    override suspend fun deleteConfigs(widgetIds: List<Int>): Result<Unit> =
        suspendRunCatching { dao.delete(widgetIds) }
}

/** 알 수 없는 값(이후 버전의 행 등)은 건너뛴다 */
private fun WidgetConfigEntity.toModel(): WidgetConfig? {
    val size = WidgetSize.entries.firstOrNull { it.name == size } ?: return null
    val background = WidgetBackground.entries.firstOrNull { it.name == backgroundType } ?: WidgetBackground.THEME
    return WidgetConfig(widgetId, size, background)
}

private fun WidgetConfig.toEntity(now: Long) = WidgetConfigEntity(widgetId, size.name, background.name, now)
