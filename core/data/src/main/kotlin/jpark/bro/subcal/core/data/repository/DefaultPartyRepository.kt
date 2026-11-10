package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.common.IdGenerator
import jpark.bro.subcal.core.common.suspendRunCatching
import jpark.bro.subcal.core.data.mapper.toEntity
import jpark.bro.subcal.core.data.mapper.toModel
import jpark.bro.subcal.core.database.dao.PartyDao
import jpark.bro.subcal.core.database.dao.SubscriptionDao
import jpark.bro.subcal.core.database.entity.PartySettlementEntity
import jpark.bro.subcal.core.domain.billing.priceOn
import jpark.bro.subcal.core.domain.party.MAX_PARTY_MEMBERS
import jpark.bro.subcal.core.domain.party.PartyMemberNameLength
import jpark.bro.subcal.core.model.PartyMember
import jpark.bro.subcal.core.model.PartySettlement
import jpark.bro.subcal.core.model.PartySplitMode
import jpark.bro.subcal.core.model.SubscriptionRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth

internal class DefaultPartyRepository(
    private val partyDao: PartyDao,
    private val subscriptionDao: SubscriptionDao,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) : PartyRepository {
    /** 인원 · 합계를 읽고 판단한 뒤 쓰므로 한 번에 하나만 실행한다 */
    private val writeMutex = Mutex()

    override fun observeSettlements(): Flow<List<PartySettlement>> =
        partyDao.observeSettlements().map { rows -> rows.map(PartySettlementEntity::toModel) }

    override suspend fun addMember(
        subscriptionId: String,
        name: String,
    ): Result<String> =
        write {
            val record = record(subscriptionId)
            require(record.partyMembers.size < MAX_PARTY_MEMBERS) { "파티 멤버는 ${MAX_PARTY_MEMBERS}명까지입니다." }
            val now = clock.millis()
            val member =
                PartyMember(
                    id = idGenerator.newId(),
                    subscriptionId = subscriptionId,
                    name = validName(name),
                    shareAmountMinor = if (record.subscription.partySplitMode == PartySplitMode.CUSTOM) 0 else null,
                    sortOrder = (record.partyMembers.maxOfOrNull { it.sortOrder } ?: -1) + 1,
                    createdAt = now,
                    updatedAt = now,
                )
            partyDao.upsertMembers(listOf(member.toEntity()))
            member.id
        }

    override suspend fun renameMember(
        memberId: String,
        name: String,
    ): Result<Unit> =
        write {
            val member = requireNotNull(partyDao.getMember(memberId)) { "멤버를 찾을 수 없습니다." }
            partyDao.upsertMembers(listOf(member.copy(name = validName(name), updatedAt = clock.millis())))
        }

    override suspend fun removeMember(memberId: String): Result<Unit> =
        write {
            requireNotNull(partyDao.getMember(memberId)) { "멤버를 찾을 수 없습니다." }
            partyDao.softDeleteMember(memberId, clock.millis())
        }

    override suspend fun setSplit(
        subscriptionId: String,
        mode: PartySplitMode,
        shares: Map<String, Long>,
    ): Result<Unit> =
        write {
            val record = record(subscriptionId)
            val now = clock.millis()
            val members =
                when (mode) {
                    PartySplitMode.EQUAL -> {
                        record.partyMembers.map { it.copy(shareAmountMinor = null, updatedAt = now) }
                    }

                    PartySplitMode.CUSTOM -> {
                        require(shares.keys == record.partyMembers.map { it.id }.toSet()) { "모든 멤버의 금액이 필요합니다." }
                        require(shares.values.all { it >= 0 }) { "금액은 0 이상이어야 합니다." }
                        val total = record.priceOn(LocalDate.now(clock)).amountMinor
                        require(shares.values.sum() <= total) { "멤버 몫의 합이 구독 금액보다 큽니다." }
                        record.partyMembers.map { it.copy(shareAmountMinor = shares.getValue(it.id), updatedAt = now) }
                    }
                }
            partyDao.saveSplit(subscriptionId, mode.name, members.map(PartyMember::toEntity), now)
        }

    override suspend fun setSettled(
        memberId: String,
        period: YearMonth,
        settled: Boolean,
    ): Result<Unit> =
        write {
            requireNotNull(partyDao.getMember(memberId)) { "멤버를 찾을 수 없습니다." }
            if (settled) {
                partyDao.upsertSettlement(PartySettlement(memberId, period, clock.millis()).toEntity())
            } else {
                partyDao.deleteSettlement(memberId, period.toString())
            }
        }

    private suspend fun record(subscriptionId: String): SubscriptionRecord =
        requireNotNull(subscriptionDao.get(subscriptionId)) { "구독을 찾을 수 없습니다." }.toModel()

    private suspend fun <T> write(block: suspend () -> T): Result<T> =
        suspendRunCatching {
            writeMutex.withLock { block() }
        }
}

/** 앞뒤 공백을 빼고 1~10자 (product-spec 4-9) */
private fun validName(name: String): String =
    name.trim().also { require(it.length in PartyMemberNameLength) { "이름은 1~10자여야 합니다." } }
