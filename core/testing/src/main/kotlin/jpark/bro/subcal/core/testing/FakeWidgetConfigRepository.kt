package jpark.bro.subcal.core.testing

import jpark.bro.subcal.core.data.repository.WidgetConfigRepository
import jpark.bro.subcal.core.model.WidgetConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** 메모리 안 위젯 설정. [failure]를 넣으면 모든 쓰기가 실패한다 */
class FakeWidgetConfigRepository(
    initial: List<WidgetConfig> = emptyList(),
) : WidgetConfigRepository {
    val configs = MutableStateFlow(initial)
    var failure: Throwable? = null

    override fun observeConfigs(): Flow<List<WidgetConfig>> = configs

    override suspend fun saveConfigIfAbsent(config: WidgetConfig): Result<Unit> =
        write { configs.update { list -> if (list.any { it.widgetId == config.widgetId }) list else list + config } }

    override suspend fun saveConfig(config: WidgetConfig): Result<Unit> =
        write { configs.update { list -> list.filterNot { it.widgetId == config.widgetId } + config } }

    override suspend fun deleteConfigs(widgetIds: List<Int>): Result<Unit> =
        write { configs.update { list -> list.filterNot { it.widgetId in widgetIds } } }

    private fun write(block: () -> Unit): Result<Unit> = failure?.let { Result.failure(it) } ?: Result.success(block())
}
