package jpark.bro.subcal.feature.trial.impl

internal sealed interface TrialAction {
    data object BackClicked : TrialAction

    /** + : 무료체험을 켠 구독 입력 */
    data object AddClicked : TrialAction

    data class TrialClicked(
        val subscriptionId: String,
    ) : TrialAction

    /** "계속 쓸래요" */
    data class KeepClicked(
        val subscriptionId: String,
    ) : TrialAction

    /** "해지했어요" */
    data class CancelledClicked(
        val subscriptionId: String,
    ) : TrialAction
}
