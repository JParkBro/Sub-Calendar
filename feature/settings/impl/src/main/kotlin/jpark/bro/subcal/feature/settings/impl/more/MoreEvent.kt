package jpark.bro.subcal.feature.settings.impl.more

internal sealed interface MoreEvent {
    data object OpenNotificationSettings : MoreEvent
}
