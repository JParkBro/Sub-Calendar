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

    data object CancelDateClicked : SubscriptionDetailAction

    data class CancelDatePicked(
        val date: LocalDate,
    ) : SubscriptionDetailAction

    data object CancelDatePickerDismissed : SubscriptionDetailAction

    data object CancelConfirmed : SubscriptionDetailAction

    data object CancelSheetDismissed : SubscriptionDetailAction

    data object UndoCancelClicked : SubscriptionDetailAction

    data object ShowAllHistoryClicked : SubscriptionDetailAction
}
