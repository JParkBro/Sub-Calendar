package jpark.bro.subcal.feature.party.impl.party

import jpark.bro.subcal.core.model.PartySplitMode
import java.time.YearMonth

internal sealed interface PartyAction {
    data object BackClicked : PartyAction

    /** 열린 시트를 닫는다 */
    data object SheetDismissed : PartyAction

    /** 정산 카드 */
    sealed interface Settlement : PartyAction

    data class PeriodSelected(
        val month: YearMonth,
    ) : Settlement

    data class SettledToggled(
        val memberId: String,
        val settled: Boolean,
    ) : Settlement

    /** 미정산 멤버 한 명에게 요청 문구 공유 */
    data class RequestClicked(
        val memberId: String,
    ) : Settlement

    /** 고른 기간의 미정산 멤버 모두에게 (한 줄에 한 명) */
    data object RequestAllClicked : Settlement

    /** 멤버 추가 · 이름 시트 */
    sealed interface Member : PartyAction

    data object AddMemberClicked : Member

    data class MemberClicked(
        val memberId: String,
    ) : Member

    data class MemberNameChanged(
        val input: String,
    ) : Member

    data object MemberSaveClicked : Member

    data object MemberDeleteClicked : Member

    /** 분담 방식 시트 */
    sealed interface Split : PartyAction

    data object SplitClicked : Split

    data class SplitModeSelected(
        val mode: PartySplitMode,
    ) : Split

    data class ShareInputChanged(
        val memberId: String,
        val input: String,
    ) : Split

    data object SplitSaveClicked : Split
}
