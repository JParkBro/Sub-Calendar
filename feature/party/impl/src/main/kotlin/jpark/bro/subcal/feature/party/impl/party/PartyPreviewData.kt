package jpark.bro.subcal.feature.party.impl.party

import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.PartySplitMode
import java.time.YearMonth

/** product-spec 9장 유튜브 프리미엄 파티: 10월 민지 · 준호 받음, 서연 미정산 */
@Suppress("MagicNumber") // 샘플 데이터
internal fun previewPartyState(withMembers: Boolean = true): PartyState {
    val share = Money(3_725, Currency.KRW)
    val names = if (withMembers) listOf("민지", "준호", "서연") else emptyList()
    val bills =
        listOf(YearMonth.of(2026, 10), YearMonth.of(2026, 9), YearMonth.of(2026, 8)).mapIndexed { index, month ->
            BillContent(
                month = month,
                isCurrent = index == 0,
                firstPaymentDate = month.atDay(14),
                total = Money(14_900, Currency.KRW),
                members = names.map { MemberSettlement(it, it, share, settled = index > 0 || it != "서연") },
            )
        }
    return PartyState(
        isLoading = false,
        content =
            PartyContent(
                subscriptionId = "youtube_premium",
                name = "유튜브 프리미엄",
                colorKey = "PINK",
                splitMode = PartySplitMode.EQUAL,
                total = Money(14_900, Currency.KRW),
                partySize = names.size + 1,
                myShare = if (withMembers) share else Money(14_900, Currency.KRW),
                members = names.map { MemberShare(it, it, share) },
                bills = if (withMembers) bills else emptyList(),
                canAddMember = true,
            ),
    )
}
