package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.model.PriceChangeTiming
import jpark.bro.subcal.core.model.ReminderMode
import jpark.bro.subcal.core.model.SubscriptionInput
import jpark.bro.subcal.core.model.SubscriptionRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SubscriptionRepository {
    /** 삭제되지 않은 구독 전체 */
    fun observeRecords(): Flow<List<SubscriptionRecord>>

    /** 삭제됐거나 없으면 null */
    fun observeRecord(id: String): Flow<SubscriptionRecord?>

    /** 새 구독의 id를 돌려준다 */
    suspend fun add(input: SubscriptionInput): Result<String>

    /** 금액이 바뀌면 [timing]대로 적용한다 (product-spec 2-5). 통화가 바뀌면 항상 처음부터 적용한다 */
    suspend fun update(
        id: String,
        input: SubscriptionInput,
        timing: PriceChangeTiming = PriceChangeTiming.NEXT_PAYMENT,
    ): Result<Unit>

    /** 이용 중인 구독을 오늘부터 일시정지한다 */
    suspend fun pause(id: String): Result<Unit>

    suspend fun resume(id: String): Result<Unit>

    suspend fun cancel(
        id: String,
        cancelledOn: LocalDate,
    ): Result<Unit>

    suspend fun undoCancel(id: String): Result<Unit>

    /**
     * 구독별 결제 알림 시점 (product-spec 2-12). [daysBefore]는 [ReminderMode.CUSTOM]일 때만 쓰고
     * [NotificationSettings.DaysBeforeOptions] 중 하나여야 한다. 다른 모드면 무시한다.
     */
    suspend fun setReminder(
        id: String,
        mode: ReminderMode,
        daysBefore: Int? = null,
    ): Result<Unit>

    /** soft delete. 잘못 등록한 구독을 지우는 용도 (product-spec 2-6) */
    suspend fun delete(id: String): Result<Unit>
}
