package jpark.bro.subcal.core.testing

import jpark.bro.subcal.core.data.repository.BackupRepository
import jpark.bro.subcal.core.model.BackupSummary

/**
 * 백업 JSON 대신 정해 둔 문자열을 주고받는다. [validJson]만 올바른 백업으로 본다.
 * [failure]를 넣으면 모든 작업이 그 예외로 실패한다.
 */
class FakeBackupRepository(
    val validJson: String = "{\"format\":\"subcal-backup\"}",
    val summary: BackupSummary = BackupSummary(subscriptionCount = 3, exportedAt = 0),
) : BackupRepository {
    var failure: Throwable? = null

    /** 마지막으로 불러온 JSON */
    var restored: String? = null
        private set

    override suspend fun export(appVersion: String): Result<String> = result { validJson }

    override suspend fun inspect(json: String): Result<BackupSummary> =
        result {
            require(json == validJson) { "백업 파일이 아닙니다." }
            summary
        }

    override suspend fun restore(json: String): Result<Unit> =
        result {
            require(json == validJson) { "백업 파일이 아닙니다." }
            restored = json
        }

    /** [deleteAll]을 불렀는지 */
    var deleted: Boolean = false
        private set

    override suspend fun deleteAll(): Result<Unit> = result { deleted = true }

    private fun <T> result(block: () -> T): Result<T> = failure?.let { Result.failure(it) } ?: runCatching(block)
}
