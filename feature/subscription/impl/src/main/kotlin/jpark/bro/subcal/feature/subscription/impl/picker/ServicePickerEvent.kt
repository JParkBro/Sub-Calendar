package jpark.bro.subcal.feature.subscription.impl.picker

internal sealed interface ServicePickerEvent {
    /** [presetId]가 null이면 직접 입력 */
    data class OpenEdit(
        val presetId: String?,
    ) : ServicePickerEvent

    data object NavigateBack : ServicePickerEvent
}
