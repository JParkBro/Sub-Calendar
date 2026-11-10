package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.common.IdGenerator
import jpark.bro.subcal.core.common.suspendRunCatching
import jpark.bro.subcal.core.data.mapper.toEntity
import jpark.bro.subcal.core.data.mapper.toModel
import jpark.bro.subcal.core.database.dao.SubscriptionDao
import jpark.bro.subcal.core.database.entity.SubscriptionWithRules
import jpark.bro.subcal.core.domain.billing.nextPayment
import jpark.bro.subcal.core.domain.billing.priceOn
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.model.Pause
import jpark.bro.subcal.core.model.PriceChange
import jpark.bro.subcal.core.model.PriceChangeTiming
import jpark.bro.subcal.core.model.ReminderMode
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionInput
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.model.TrialOutcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.LocalDate

internal class DefaultSubscriptionRepository(
    private val dao: SubscriptionDao,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) : SubscriptionRepository {
    /** 읽고 판단한 뒤 쓰는 작업이 겹치면 일시정지가 두 번 생길 수 있어 한 번에 하나만 실행한다 */
    private val writeMutex = Mutex()

    private val today: LocalDate get() = LocalDate.now(clock)

    override fun observeRecords(): Flow<List<SubscriptionRecord>> =
        dao.observeAll().map { rows -> rows.map(SubscriptionWithRules::toModel) }

    override fun observeRecord(id: String): Flow<SubscriptionRecord?> = dao.observe(id).map { it?.toModel() }

    override suspend fun add(input: SubscriptionInput): Result<String> =
        suspendRunCatching {
            val id = idGenerator.newId()
            val now = clock.millis()
            dao.upsertSubscription(
                Subscription(
                    id = id,
                    name = input.name,
                    amount = input.amount,
                    cycle = input.cycle,
                    firstBillingDate = input.firstBillingDate,
                    colorKey = input.colorKey,
                    createdAt = now,
                    updatedAt = now,
                ).withInput(input, now).toEntity(),
            )
            id
        }

    override suspend fun update(
        id: String,
        input: SubscriptionInput,
        timing: PriceChangeTiming,
    ): Result<Unit> =
        dao.edit(writeMutex, id) { record ->
            val now = clock.millis()
            val edited = record.subscription.withInput(input, now)
            val keepsPrice = input.amount == record.priceOn(today)
            when {
                keepsPrice -> {
                    dao.save(edited.toEntity())
                }

                timing == PriceChangeTiming.FROM_START || input.amount.currency != edited.amount.currency -> {
                    dao.save(edited.copy(amount = input.amount).toEntity(), clearPriceChanges = true)
                }

                else -> {
                    val change =
                        record
                            .copy(
                                subscription = edited,
                            ).priceChangeFromNextPayment(input.amount, today, now, idGenerator)
                    dao.save(edited.toEntity(), priceChange = change.toEntity())
                }
            }
        }

    override suspend fun pause(id: String): Result<Unit> =
        dao.edit(writeMutex, id) { record ->
            require(record.subscription.status == SubscriptionStatus.ACTIVE) { "이용 중인 구독만 일시정지할 수 있습니다." }
            require(record.pauses.none { it.endDate?.isBefore(today) != true }) { "일시정지 기간이 겹칩니다." }
            val now = clock.millis()
            dao.save(
                record.subscription.copy(status = SubscriptionStatus.PAUSED, updatedAt = now).toEntity(),
                pauseToSave = Pause(idGenerator.newId(), id, startDate = today, createdAt = now).toEntity(),
            )
        }

    override suspend fun resume(id: String): Result<Unit> =
        dao.edit(writeMutex, id) { record ->
            require(record.subscription.status == SubscriptionStatus.PAUSED) { "일시정지한 구독만 다시 시작할 수 있습니다." }
            val updated = record.subscription.copy(status = SubscriptionStatus.ACTIVE, updatedAt = clock.millis())
            dao.saveClosingPause(updated, record.openPause(), today.minusDays(1))
        }

    override suspend fun cancel(
        id: String,
        cancelledOn: LocalDate,
    ): Result<Unit> =
        dao.edit(writeMutex, id) { record ->
            val subscription = record.subscription
            require(subscription.status != SubscriptionStatus.CANCELLED) { "이미 해지한 구독입니다." }
            val updated =
                subscription.copy(
                    status = SubscriptionStatus.CANCELLED,
                    cancelledOn = cancelledOn,
                    trialOutcome =
                        if (subscription.trialOutcome == TrialOutcome.PENDING) {
                            TrialOutcome.CANCELLED
                        } else {
                            subscription.trialOutcome
                        },
                    updatedAt = clock.millis(),
                )
            dao.saveClosingPause(updated, record.openPause(), cancelledOn.minusDays(1))
        }

    override suspend fun undoCancel(id: String): Result<Unit> =
        dao.edit(writeMutex, id) { record ->
            val subscription = record.subscription
            require(subscription.status == SubscriptionStatus.CANCELLED) { "해지한 구독만 취소할 수 있습니다." }
            dao.upsertSubscription(
                subscription
                    .copy(
                        status = SubscriptionStatus.ACTIVE,
                        cancelledOn = null,
                        trialOutcome =
                            if (subscription.trialOutcome == TrialOutcome.CANCELLED) {
                                TrialOutcome.PENDING
                            } else {
                                subscription.trialOutcome
                            },
                        updatedAt = clock.millis(),
                    ).toEntity(),
            )
        }

    override suspend fun setReminder(
        id: String,
        mode: ReminderMode,
        daysBefore: Int?,
    ): Result<Unit> =
        dao.edit(writeMutex, id) { record ->
            val days = if (mode == ReminderMode.CUSTOM) daysBefore else null
            require(mode != ReminderMode.CUSTOM || days in NotificationSettings.DaysBeforeOptions) {
                "고를 수 없는 알림 시점입니다."
            }
            dao.upsertSubscription(
                record.subscription
                    .copy(reminderMode = mode, reminderDaysBefore = days, updatedAt = clock.millis())
                    .toEntity(),
            )
        }

    override suspend fun keepTrial(id: String): Result<Unit> =
        dao.edit(writeMutex, id) { record ->
            val subscription = record.subscription
            require(subscription.trialOutcome == TrialOutcome.PENDING) { "결과를 고르기 전인 체험만 고를 수 있습니다." }
            dao.upsertSubscription(
                subscription.copy(trialOutcome = TrialOutcome.KEPT, updatedAt = clock.millis()).toEntity(),
            )
        }

    override suspend fun setConsiderCancel(
        id: String,
        considerCancel: Boolean,
    ): Result<Unit> =
        dao.edit(writeMutex, id) { record ->
            dao.upsertSubscription(
                record.subscription.copy(considerCancel = considerCancel, updatedAt = clock.millis()).toEntity(),
            )
        }

    override suspend fun delete(id: String): Result<Unit> =
        dao.edit(writeMutex, id) { dao.softDelete(id, clock.millis()) }
}

/** 한 번에 하나씩: [id] 구독을 읽어 [block]으로 고친다. 없으면 실패 */
private suspend fun SubscriptionDao.edit(
    mutex: Mutex,
    id: String,
    block: suspend (SubscriptionRecord) -> Unit,
): Result<Unit> =
    suspendRunCatching {
        mutex.withLock {
            block(requireNotNull(get(id)) { "구독을 찾을 수 없습니다." }.toModel())
        }
    }

/** 다음 결제일부터 적용할 가격. 그 날짜에 이미 변경이 있으면 그 행을 고쳐 쓴다. 다음 결제가 없으면 [today] */
private fun SubscriptionRecord.priceChangeFromNextPayment(
    amount: Money,
    today: LocalDate,
    now: Long,
    idGenerator: IdGenerator,
): PriceChange {
    val effectiveFrom = nextPayment(today)?.date ?: today
    return PriceChange(
        id = priceChanges.firstOrNull { it.effectiveFrom == effectiveFrom }?.id ?: idGenerator.newId(),
        subscriptionId = subscription.id,
        effectiveFrom = effectiveFrom,
        amountMinor = amount.amountMinor,
        createdAt = now,
    )
}

/** 열린 일시정지를 [end]에 닫으며 저장한다. 시작일보다 앞이면 정지된 날이 없으므로 행을 지운다 */
private suspend fun SubscriptionDao.saveClosingPause(
    subscription: Subscription,
    open: Pause?,
    end: LocalDate,
) {
    val isEmptyPause = open != null && end.isBefore(open.startDate)
    save(
        subscription.toEntity(),
        pauseToSave = open?.takeUnless { isEmptyPause }?.copy(endDate = end)?.toEntity(),
        pauseIdToDelete = open?.id?.takeIf { isEmptyPause },
    )
}

private fun SubscriptionRecord.openPause(): Pause? = pauses.firstOrNull { it.endDate == null }

/** 입력값을 반영한다. 기본 금액은 호출하는 쪽이 정한다 (가격 변경이면 그대로 둔다) */
private fun Subscription.withInput(
    input: SubscriptionInput,
    now: Long,
): Subscription =
    copy(
        presetId = input.presetId,
        name = input.name,
        planName = input.planName,
        category = input.category,
        cycle = input.cycle,
        firstBillingDate = input.firstBillingDate,
        trialStartedOn = input.trialStartedOn,
        trialOutcome =
            when {
                input.trialStartedOn == null -> TrialOutcome.NONE
                trialStartedOn == null -> TrialOutcome.PENDING
                else -> trialOutcome
            },
        paymentMethod = input.paymentMethod,
        reminderMode = input.reminderMode,
        reminderDaysBefore = input.reminderDaysBefore,
        colorKey = input.colorKey,
        cancelUrl = input.cancelUrl,
        memo = input.memo,
        trackUsage = input.trackUsage,
        updatedAt = now,
    )
