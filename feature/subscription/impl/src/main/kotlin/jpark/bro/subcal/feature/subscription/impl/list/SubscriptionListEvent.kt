package jpark.bro.subcal.feature.subscription.impl.list

internal sealed interface SubscriptionListEvent {
    data object OpenServicePicker : SubscriptionListEvent

    data class OpenDetail(
        val id: String,
    ) : SubscriptionListEvent
}
