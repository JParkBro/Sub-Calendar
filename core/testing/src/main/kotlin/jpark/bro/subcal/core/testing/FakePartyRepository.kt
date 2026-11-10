package jpark.bro.subcal.core.testing

import jpark.bro.subcal.core.data.repository.PartyRepository
import jpark.bro.subcal.core.model.PartyMember
import jpark.bro.subcal.core.model.PartySettlement
import jpark.bro.subcal.core.model.PartySplitMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.YearMonth

/**
 * 메모리 안 파티. 멤버는 [subscriptions]의 구독에 붙여 [FakeSubscriptionRepository.records]로 나온다.
 * 새 멤버 id는 "member-1"부터. [failure]를 넣으면 모든 쓰기가 실패한다. 이름 · 인원 · 금액 검사는 하지 않는다.
 */
class FakePartyRepository(
    private val subscriptions: FakeSubscriptionRepository,
    initialSettlements: List<PartySettlement> = emptyList(),
) : PartyRepository {
    val settlements = MutableStateFlow(initialSettlements)
    var failure: Throwable? = null
    private var nextId = 0

    override fun observeSettlements(): Flow<List<PartySettlement>> = settlements

    override suspend fun addMember(
        subscriptionId: String,
        name: String,
    ): Result<String> =
        write {
            val id = "member-${++nextId}"
            editMembers(subscriptionId) { members ->
                members + PartyMember(id, subscriptionId, name.trim(), null, members.size, 0, 0)
            }
            id
        }

    override suspend fun renameMember(
        memberId: String,
        name: String,
    ): Result<Unit> = write { editMember(memberId) { it.copy(name = name.trim()) } }

    override suspend fun removeMember(memberId: String): Result<Unit> =
        write {
            subscriptions.records.update { records ->
                records.map { record ->
                    record.copy(partyMembers = record.partyMembers.filterNot { it.id == memberId })
                }
            }
        }

    override suspend fun setSplit(
        subscriptionId: String,
        mode: PartySplitMode,
        shares: Map<String, Long>,
    ): Result<Unit> =
        write {
            subscriptions.records.update { records ->
                records.map { record ->
                    if (record.subscription.id != subscriptionId) return@map record
                    record.copy(
                        subscription = record.subscription.copy(partySplitMode = mode),
                        partyMembers =
                            record.partyMembers.map {
                                it.copy(shareAmountMinor = if (mode == PartySplitMode.CUSTOM) shares[it.id] else null)
                            },
                    )
                }
            }
        }

    override suspend fun setSettled(
        memberId: String,
        period: YearMonth,
        settled: Boolean,
    ): Result<Unit> =
        write {
            settlements.update { list ->
                val others = list.filterNot { it.partyMemberId == memberId && it.period == period }
                if (settled) others + PartySettlement(memberId, period, 0) else others
            }
        }

    private fun editMembers(
        subscriptionId: String,
        edit: (List<PartyMember>) -> List<PartyMember>,
    ) {
        subscriptions.records.update { records ->
            records.map {
                if (it.subscription.id ==
                    subscriptionId
                ) {
                    it.copy(partyMembers = edit(it.partyMembers))
                } else {
                    it
                }
            }
        }
    }

    private fun editMember(
        memberId: String,
        edit: (PartyMember) -> PartyMember,
    ) {
        subscriptions.records.update { records ->
            records.map { record ->
                record.copy(partyMembers = record.partyMembers.map { if (it.id == memberId) edit(it) else it })
            }
        }
    }

    private fun <T> write(block: () -> T): Result<T> = failure?.let { Result.failure(it) } ?: Result.success(block())
}
