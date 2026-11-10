package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.model.WidgetConfig
import kotlinx.coroutines.flow.Flow

/** 홈 화면 위젯 설정 (data-model-erd 3장 `widget_config`). 위젯 모듈과 위젯 설정 화면이 쓴다 */
interface WidgetConfigRepository {
    fun observeConfigs(): Flow<List<WidgetConfig>>

    /** 위젯을 놓았을 때 기본값 저장. 이미 있으면 그대로 */
    suspend fun saveConfigIfAbsent(config: WidgetConfig): Result<Unit>

    suspend fun saveConfig(config: WidgetConfig): Result<Unit>

    /** 위젯을 지우면 설정도 지운다 */
    suspend fun deleteConfigs(widgetIds: List<Int>): Result<Unit>
}
