package jpark.bro.subcal.core.testing

import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.model.PriceChangeTiming
import jpark.bro.subcal.core.model.ReminderMode
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionInput
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.model.TrialOutcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

/**
 * 메모리 안 구독 목록. [failure]를 넣으면 모든 쓰기가 그 예외로 실패한다.
 * 가격 변경 · 일시정지 기간 계산은 실제 Repository 테스트가 맡고, 여기서는 상태만 바꾼다.
 */
class FakeSubscriptionRepository(
    initial: List<SubscriptionRecord> = emptyList(),
) : SubscriptionRepository {
    val records = MutableStateFlow(initial)
    var failure: Throwable? = null

    /** 마지막 [update]의 적용 시점 */
    var lastUpdateTiming: PriceChangeTiming? = null
    private var nextId = 1

    override fun observeRecords(): Flow<List<SubscriptionRecord>> = records

    override fun observeRecord(id: String): Flow<SubscriptionRecord?> =
        records.map { list -> list.firstOrNull { it.subscription.id == id } }

    override suspend fun add(input: SubscriptionInput): Result<String> =
        fakeResult(failure) {
            val id = "fake-${nextId++}"
            records.update { it + SubscriptionRecord(input.toSubscription(id)) }
            id
        }

    override suspend fun update(
        id: String,
        input: SubscriptionInput,
        timing: PriceChangeTiming,
    ): Result<Unit> {
        lastUpdateTiming = timing
        return fakeResult(failure) {
            records.update { it.edit(id) { input.toSubscription(id).copy(status = status, cancelledOn = cancelledOn) } }
        }
    }

    override suspend fun pause(id: String): Result<Unit> =
        fakeResult(failure) {
            records.update { it.edit(id) { copy(status = SubscriptionStatus.PAUSED) } }
        }

    override suspend fun resume(id: String): Result<Unit> =
        fakeResult(failure) {
            records.update { it.edit(id) { copy(status = SubscriptionStatus.ACTIVE) } }
        }

    override suspend fun cancel(
        id: String,
        cancelledOn: LocalDate,
    ): Result<Unit> =
        fakeResult(failure) {
            records.update { it.edit(id) { copy(status = SubscriptionStatus.CANCELLED, cancelledOn = cancelledOn) } }
        }

    override suspend fun undoCancel(id: String): Result<Unit> =
        fakeResult(
            failure,
        ) { records.update { it.edit(id) { copy(status = SubscriptionStatus.ACTIVE, cancelledOn = null) } } }

    override suspend fun setReminder(
        id: String,
        mode: ReminderMode,
        daysBefore: Int?,
    ): Result<Unit> =
        fakeResult(failure) {
            val days = if (mode == ReminderMode.CUSTOM) daysBefore else null
            records.update { it.edit(id) { copy(reminderMode = mode, reminderDaysBefore = days) } }
        }

    override suspend fun delete(id: String): Result<Unit> =
        fakeResult(failure) { records.update { list -> list.filterNot { it.subscription.id == id } } }
}

private fun <T> fakeResult(
    failure: Throwable?,
    block: () -> T,
): Result<T> = failure?.let { Result.failure(it) } ?: Result.success(block())

private fun List<SubscriptionRecord>.edit(
    id: String,
    change: Subscription.() -> Subscription,
): List<SubscriptionRecord> =
    map {
        if (it.subscription.id ==
            id
        ) {
            it.copy(subscription = it.subscription.change())
        } else {
            it
        }
    }

private fun SubscriptionInput.toSubscription(id: String): Subscription =
    Subscription(
        id = id,
        presetId = presetId,
        name = name,
        planName = planName,
        category = category,
        amount = amount,
        cycle = cycle,
        firstBillingDate = firstBillingDate,
        trialStartedOn = trialStartedOn,
        trialOutcome = if (trialStartedOn != null) TrialOutcome.PENDING else TrialOutcome.NONE,
        paymentMethod = paymentMethod,
        reminderMode = reminderMode,
        reminderDaysBefore = reminderDaysBefore,
        colorKey = colorKey,
        cancelUrl = cancelUrl,
        memo = memo,
        trackUsage = trackUsage,
        createdAt = 0,
        updatedAt = 0,
    )
