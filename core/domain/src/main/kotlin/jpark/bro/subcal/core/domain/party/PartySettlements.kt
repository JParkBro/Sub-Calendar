package jpark.bro.subcal.core.domain.party

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.billing.isPendingTrial
import jpark.bro.subcal.core.domain.billing.payments
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.model.PartyMember
import jpark.bro.subcal.core.model.PartySettlement
import jpark.bro.subcal.core.model.SubscriptionRecord
import java.time.LocalDate

/** 이번 달 기간 말고 지난 정산을 몇 개 기간까지 보여줄지 (product-spec 4-9 "3개월까지") */
const val PAST_SETTLEMENT_PERIODS = 3

private const val HANGUL_FIRST = 0xAC00
private const val HANGUL_LAST = 0xD7A3
private const val HANGUL_FINALS = 28

/** 정산할 한 기간: 그 기간 결제 합 (보통 한 번) */
data class PartyBill(
    val period: BillingPeriod,
    /** 기간 안 첫 결제일 */
    val firstPaymentDate: LocalDate,
    val total: Money,
)

/** 정산 알림 한 건 (product-spec 4-9): 어제 결제한 파티 구독과 아직 안 보낸 멤버 */
data class PartyReminder(
    val record: SubscriptionRecord,
    val period: BillingPeriod,
    val unsettledMembers: List<PartyMember>,
)

/**
 * 정산할 기간 (product-spec 4-9): 이번 달 기간과 지난 [PAST_SETTLEMENT_PERIODS]개 기간 중 결제가 있는 것, 최근 순.
 * 이번 달 기간의 결제는 아직 오지 않았어도 넣는다. 결과를 고르기 전인 체험은 결제가 없다.
 */
fun SubscriptionRecord.partyBills(
    today: LocalDate,
    monthStartDay: Int,
): List<PartyBill> {
    if (subscription.isPendingTrial(today)) return emptyList()
    val current = BillingPeriod.containing(today, monthStartDay)
    return generateSequence(current) { it.previous() }
        .take(PAST_SETTLEMENT_PERIODS + 1)
        .mapNotNull { period ->
            val payments = payments(period.start, period.endExclusive)
            payments.firstOrNull()?.let { first ->
                PartyBill(
                    period = period,
                    firstPaymentDate = first.date,
                    total = first.amount.copy(amountMinor = payments.sumOf { it.amount.amountMinor }),
                )
            }
        }.toList()
}

/** [member]가 [period]를 보냈는지 */
fun List<PartySettlement>.isSettled(
    member: PartyMember,
    period: BillingPeriod,
): Boolean = any { it.partyMemberId == member.id && it.period == period.month }

/**
 * [today]에 보낼 정산 알림 (product-spec 4-9): 어제 결제한 파티 구독 중 그 기간에 "받음"이 아닌 멤버가 있는 것.
 * 정산 알림을 끄면 없다. 이름순.
 */
fun partyReminders(
    records: List<SubscriptionRecord>,
    settlements: List<PartySettlement>,
    settings: NotificationSettings,
    monthStartDay: Int,
    today: LocalDate,
): List<PartyReminder> {
    if (!settings.partyEnabled) return emptyList()
    val yesterday = today.minusDays(1)
    return records
        .filter { it.hasParty && !it.subscription.isPendingTrial(yesterday) }
        .filter { it.payments(yesterday, today).isNotEmpty() }
        .mapNotNull { record ->
            val period = BillingPeriod.containing(yesterday, monthStartDay)
            record.partyMembers
                .filterNot { settlements.isSettled(it, period) }
                .takeIf { it.isNotEmpty() }
                ?.let { PartyReminder(record, period, it) }
        }.sortedBy { it.record.subscription.name }
}

/** 부르는 말 뒤 조사: 받침이 있으면 "아", 없거나 한글이 아니면 "야" ("민지야", "서연아") */
fun vocativeParticle(name: String): String {
    val last = name.trim().lastOrNull()?.code ?: return "야"
    val hasFinal = last in HANGUL_FIRST..HANGUL_LAST && (last - HANGUL_FIRST) % HANGUL_FINALS != 0
    return if (hasFinal) "아" else "야"
}
