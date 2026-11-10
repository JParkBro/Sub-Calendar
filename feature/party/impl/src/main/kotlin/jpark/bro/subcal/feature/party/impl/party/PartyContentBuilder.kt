package jpark.bro.subcal.feature.party.impl.party

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.billing.priceOn
import jpark.bro.subcal.core.domain.party.MAX_PARTY_MEMBERS
import jpark.bro.subcal.core.domain.party.isSettled
import jpark.bro.subcal.core.domain.party.memberShare
import jpark.bro.subcal.core.domain.party.myShare
import jpark.bro.subcal.core.domain.party.partyBills
import jpark.bro.subcal.core.domain.party.partySize
import jpark.bro.subcal.core.model.PartySettlement
import jpark.bro.subcal.core.model.SubscriptionRecord
import java.time.LocalDate

/** 파티 정산 화면 값 (product-spec 4-9). 계산은 core:domain */
internal fun buildPartyContent(
    record: SubscriptionRecord,
    settlements: List<PartySettlement>,
    monthStartDay: Int,
    today: LocalDate,
): PartyContent {
    val subscription = record.subscription
    val total = record.priceOn(today)
    val current = BillingPeriod.containing(today, monthStartDay)
    return PartyContent(
        subscriptionId = subscription.id,
        name = subscription.name,
        colorKey = subscription.colorKey,
        splitMode = subscription.partySplitMode,
        total = total,
        partySize = record.partySize,
        myShare = record.myShare(total),
        members = record.partyMembers.map { MemberShare(it.id, it.name, record.memberShare(it, total)) },
        bills =
            if (record.partyMembers.isEmpty()) {
                emptyList()
            } else {
                record.partyBills(today, monthStartDay).map { bill ->
                    BillContent(
                        month = bill.period.month,
                        isCurrent = bill.period == current,
                        firstPaymentDate = bill.firstPaymentDate,
                        total = bill.total,
                        members =
                            record.partyMembers.map { member ->
                                MemberSettlement(
                                    id = member.id,
                                    name = member.name,
                                    share = record.memberShare(member, bill.total),
                                    settled = settlements.isSettled(member, bill.period),
                                )
                            },
                    )
                }
            },
        canAddMember = record.partyMembers.size < MAX_PARTY_MEMBERS,
    )
}
