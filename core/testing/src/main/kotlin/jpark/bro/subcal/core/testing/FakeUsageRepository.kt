package jpark.bro.subcal.core.testing

import jpark.bro.subcal.core.data.repository.UsageRepository
import jpark.bro.subcal.core.model.UsageLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

/** 메모리 안 사용 기록. id는 "usage-1"부터. [failure]를 넣으면 모든 쓰기가 실패한다 */
class FakeUsageRepository(
    initial: List<UsageLog> = emptyList(),
) : UsageRepository {
    val logs = MutableStateFlow(initial)
    var failure: Throwable? = null
    private var nextId = 0

    override fun observeUsageLogs(): Flow<List<UsageLog>> = logs

    override suspend fun add(
        subscriptionId: String,
        usedOn: LocalDate,
    ): Result<String> {
        failure?.let { return Result.failure(it) }
        val id = "usage-${++nextId}"
        logs.update { list -> (list + UsageLog(id, subscriptionId, usedOn, createdAt = 0)).sortedBy { it.usedOn } }
        return Result.success(id)
    }

    override suspend fun remove(id: String): Result<Unit> {
        failure?.let { return Result.failure(it) }
        logs.update { list -> list.filterNot { it.id == id } }
        return Result.success(Unit)
    }
}
