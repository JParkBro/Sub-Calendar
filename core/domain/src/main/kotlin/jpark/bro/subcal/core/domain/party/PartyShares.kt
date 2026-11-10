package jpark.bro.subcal.core.domain.party

import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.PartyMember
import jpark.bro.subcal.core.model.PartySplitMode
import jpark.bro.subcal.core.model.SubscriptionRecord

/** 멤버는 나를 빼고 최대 9명 (product-spec 4-9) */
const val MAX_PARTY_MEMBERS = 9

private const val MAX_MEMBER_NAME_LENGTH = 10

/** 멤버 이름 1~10자 (product-spec 4-9) */
val PartyMemberNameLength: IntRange = 1..MAX_MEMBER_NAME_LENGTH

/** 파티 인원: 멤버 + 나 */
val SubscriptionRecord.partySize: Int get() = partyMembers.size + 1

val SubscriptionRecord.hasParty: Boolean get() = partyMembers.isNotEmpty()

/**
 * 결제 [amount] 중 [member]의 몫 (product-spec 4-9).
 * 균등: 총액 ÷ 인원, 최소 단위 내림. 직접 입력: 저장한 금액(없으면 0).
 */
fun SubscriptionRecord.memberShare(
    member: PartyMember,
    amount: Money,
): Money =
    when (subscription.partySplitMode) {
        PartySplitMode.EQUAL -> amount.copy(amountMinor = amount.amountMinor / partySize)
        PartySplitMode.CUSTOM -> amount.copy(amountMinor = member.shareAmountMinor ?: 0)
    }

/**
 * 결제 [amount] 중 내 몫 = 총액 − 다른 멤버 몫 합. 균등이면 내림하고 남는 금액이 나에게 온다.
 * 직접 입력 합이 가격 변경으로 총액을 넘으면 0이다. 파티가 아니면 총액 그대로.
 */
fun SubscriptionRecord.myShare(amount: Money): Money {
    val others = partyMembers.sumOf { memberShare(it, amount).amountMinor }
    return amount.copy(amountMinor = (amount.amountMinor - others).coerceAtLeast(0))
}

/**
 * 결제 금액을 모두 내 몫으로 바꾼 구독 (통계 · 가성비의 "내 부담액", product-spec 4-6 · 4-10).
 * 기본 금액과 가격 변경마다 [myShare]를 적용하므로 결제 · 월 환산 계산을 그대로 쓸 수 있다.
 * 멤버 구성은 지금 기준이라 지난 결제에도 같은 분담을 적용한다.
 */
fun SubscriptionRecord.toMyShare(): SubscriptionRecord {
    if (!hasParty) return this
    val currency = subscription.amount.currency
    return copy(
        subscription = subscription.copy(amount = myShare(subscription.amount)),
        priceChanges =
            priceChanges.map { change ->
                change.copy(amountMinor = myShare(Money(change.amountMinor, currency)).amountMinor)
            },
    )
}
