package jpark.bro.subcal.feature.settings.impl.more

internal sealed interface MoreAction {
    data object NotificationSettingsClicked : MoreAction
}
