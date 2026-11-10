package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.model.UsageLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** 사용 체크 기록 (product-spec 4-4, 4-10) */
interface UsageRepository {
    /** 삭제되지 않은 구독의 사용 기록 전체, 날짜순 */
    fun observeUsageLogs(): Flow<List<UsageLog>>

    /** [usedOn]은 오늘까지만. 새 기록의 id를 돌려준다 */
    suspend fun add(
        subscriptionId: String,
        usedOn: LocalDate,
    ): Result<String>

    /** 실행 취소: 기록을 soft delete한다 */
    suspend fun remove(id: String): Result<Unit>
}
