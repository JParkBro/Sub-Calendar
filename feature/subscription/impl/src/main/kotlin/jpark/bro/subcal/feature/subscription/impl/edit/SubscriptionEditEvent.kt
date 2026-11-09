package jpark.bro.subcal.feature.subscription.impl.edit

internal sealed interface SubscriptionEditEvent {
    /** 취소, 또는 수정할 구독이 없을 때 */
    data object NavigateBack : SubscriptionEditEvent

    data object Saved : SubscriptionEditEvent

    data object Deleted : SubscriptionEditEvent

    data object SaveFailed : SubscriptionEditEvent

    data object DeleteFailed : SubscriptionEditEvent
}
