package jpark.bro.subcal.feature.subscription.impl.detail

import java.time.LocalDate

internal sealed interface SubscriptionDetailAction {
    data object EditClicked : SubscriptionDetailAction

    data object PauseClicked : SubscriptionDetailAction

    data object ResumeClicked : SubscriptionDetailAction

    /** 이용 중의 "해지하러 가기", 일시정지의 "해지" */
    data object CancelClicked : SubscriptionDetailAction

    /** 화면으로 돌아왔을 때 (해지 페이지에서 복귀) */
    data object Resumed : SubscriptionDetailAction

    /** "해지했나요?" 시트 */
    sealed interface CancelSheet : SubscriptionDetailAction

    data object CancelDateClicked : CancelSheet

    data class CancelDatePicked(
        val date: LocalDate,
    ) : CancelSheet

    data object CancelDatePickerDismissed : CancelSheet

    data object CancelConfirmed : CancelSheet

    data object CancelSheetDismissed : CancelSheet

    data object UndoCancelClicked : SubscriptionDetailAction

    data object ShowAllHistoryClicked : SubscriptionDetailAction

    /** 파티 행 → 파티 정산 */
    data object PartyClicked : SubscriptionDetailAction

    /** 헤더 메뉴 */
    sealed interface Menu : SubscriptionDetailAction

    data object MenuClicked : Menu

    data object MenuDismissed : Menu

    /** 해지 고민함에 담기 · 빼기 (product-spec 4-4) */
    data object ConsiderCancelToggled : Menu

    /** 사용 체크 카드 (product-spec 4-4) */
    sealed interface Usage : SubscriptionDetailAction

    /** "+1": 오늘 날짜로 기록 */
    data object UsageAddClicked : Usage

    /** "+1" 길게 누르기: 날짜 선택 */
    data object UsageAddLongClicked : Usage

    data class UsageDatePicked(
        val date: LocalDate,
    ) : Usage

    data object UsageDatePickerDismissed : Usage

    /** 기록 직후 토스트의 "실행 취소" */
    data class UsageUndoClicked(
        val usageLogId: String,
    ) : Usage
}
