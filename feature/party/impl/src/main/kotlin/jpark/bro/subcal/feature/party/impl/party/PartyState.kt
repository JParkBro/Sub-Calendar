package jpark.bro.subcal.feature.party.impl.party

import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.PartySplitMode
import java.time.LocalDate
import java.time.YearMonth

/**
 * 파티 정산 (product-spec 4-9). 시안의 "결제자" 표시 · 문구 수정 · 카카오 버튼 · 자동 정산은 넣지 않는다 (11장).
 */
internal data class PartyState(
    val isLoading: Boolean = true,
    /** 구독을 읽은 뒤에만 있다 */
    val content: PartyContent? = null,
    /** 고른 정산 기간. null이거나 목록에 없으면 가장 최근 */
    val selectedMonth: YearMonth? = null,
    val sheet: PartySheet? = null,
    /** 저장 중 */
    val isWorking: Boolean = false,
)

internal data class PartyContent(
    val subscriptionId: String,
    val name: String,
    val colorKey: String,
    val splitMode: PartySplitMode,
    /** 오늘 유효한 구독 금액 */
    val total: Money,
    /** 나 포함 인원 */
    val partySize: Int,
    val myShare: Money,
    /** 나를 뺀 멤버, 추가한 순 */
    val members: List<MemberShare>,
    /** 정산할 기간, 최근 순 (이번 달 + 지난 3개 기간 중 결제가 있는 것) */
    val bills: List<BillContent>,
    val canAddMember: Boolean,
)

internal data class MemberShare(
    val id: String,
    val name: String,
    /** 오늘 금액 기준 몫 */
    val share: Money,
)

internal data class BillContent(
    val month: YearMonth,
    /** 이번 달 기간이면 요청 문구가 "이번 달" */
    val isCurrent: Boolean,
    val firstPaymentDate: LocalDate,
    val total: Money,
    val members: List<MemberSettlement>,
) {
    val settledCount: Int get() = members.count { it.settled }
    val unsettled: List<MemberSettlement> get() = members.filterNot { it.settled }
}

internal data class MemberSettlement(
    val id: String,
    val name: String,
    /** 그 기간 결제 기준 몫 */
    val share: Money,
    val settled: Boolean,
)

internal sealed interface PartySheet {
    /** 멤버 추가([memberId] null) · 이름 바꾸기 · 지우기 */
    data class MemberName(
        val memberId: String?,
        val input: String,
    ) : PartySheet

    /** 분담 방식. [inputs]는 직접 입력일 때 멤버 id → SubCalAmountField 원문 */
    data class Split(
        val mode: PartySplitMode,
        val inputs: Map<String, String>,
    ) : PartySheet
}

/** 고른 기간의 정산. 고른 기간이 없으면 가장 최근 */
internal val PartyState.selectedBill: BillContent?
    get() = content?.bills?.let { bills -> bills.firstOrNull { it.month == selectedMonth } ?: bills.firstOrNull() }
