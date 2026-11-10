package jpark.bro.subcal.feature.party.impl.party

import jpark.bro.subcal.core.model.Money

internal sealed interface PartyEvent {
    /** 뒤로, 또는 보던 구독이 삭제됐을 때 */
    data object NavigateBack : PartyEvent

    /** 요청 문구를 공유 시트로 (product-spec 4-9). Root가 문구를 만든다 */
    data class ShareRequest(
        val serviceName: String,
        /** null이면 "이번 달", 아니면 그 달 */
        val pastMonth: Int?,
        val lines: List<RequestLine>,
    ) : PartyEvent

    /** 직접 입력한 멤버 몫의 합이 구독 금액보다 크다 */
    data object SplitOverTotal : PartyEvent

    data object ActionFailed : PartyEvent
}

internal data class RequestLine(
    val name: String,
    val amount: Money,
)
