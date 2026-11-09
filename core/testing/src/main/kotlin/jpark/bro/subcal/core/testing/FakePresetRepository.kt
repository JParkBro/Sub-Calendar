package jpark.bro.subcal.core.testing

import jpark.bro.subcal.core.data.repository.PresetRepository
import jpark.bro.subcal.core.model.PresetCatalog

/** [failure]를 넣으면 목록 읽기가 실패한다 */
class FakePresetRepository(
    private val catalog: PresetCatalog = SampleData.catalog,
) : PresetRepository {
    var failure: Throwable? = null

    override suspend fun getCatalog(): Result<PresetCatalog> =
        failure?.let { Result.failure(it) } ?: Result.success(catalog)
}
