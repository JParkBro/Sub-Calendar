package jpark.bro.subcal.feature.settings.impl.moreapps

internal sealed interface MoreAppsEvent {
    /** Play 스토어 앱으로 연다. 없으면 브라우저 */
    data class OpenStore(
        val url: String,
    ) : MoreAppsEvent

    data object NavigateBack : MoreAppsEvent
}
