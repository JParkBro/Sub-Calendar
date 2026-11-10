package jpark.bro.subcal.feature.subscription.impl.edit

internal sealed interface SubscriptionEditEvent {
    /** 취소, 또는 수정할 구독이 없을 때 */
    data object NavigateBack : SubscriptionEditEvent

    /** [isNew]면 새 구독: 알림 권한이 없으면 묻고 나서 돌아간다 */
    data class Saved(
        val isNew: Boolean,
    ) : SubscriptionEditEvent

    data object Deleted : SubscriptionEditEvent

    data object SaveFailed : SubscriptionEditEvent

    data object DeleteFailed : SubscriptionEditEvent
}
