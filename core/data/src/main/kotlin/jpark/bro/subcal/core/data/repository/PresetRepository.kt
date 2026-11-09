package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.common.suspendRunCatching
import jpark.bro.subcal.core.data.preset.parsePresetCatalog
import jpark.bro.subcal.core.model.PresetCatalog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface PresetRepository {
    /** 앱에 들어 있는 서비스 목록 */
    suspend fun getCatalog(): Result<PresetCatalog>
}

internal class DefaultPresetRepository(
    private val readJson: () -> String,
    private val ioDispatcher: CoroutineDispatcher,
) : PresetRepository {
    private val catalog by lazy { parsePresetCatalog(readJson()) }

    override suspend fun getCatalog(): Result<PresetCatalog> =
        suspendRunCatching { withContext(ioDispatcher) { catalog } }
}
