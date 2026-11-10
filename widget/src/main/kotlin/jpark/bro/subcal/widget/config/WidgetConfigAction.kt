package jpark.bro.subcal.widget.config

import jpark.bro.subcal.core.model.WidgetBackground

internal sealed interface WidgetConfigAction {
    data class BackgroundSelected(
        val background: WidgetBackground,
    ) : WidgetConfigAction

    data object SaveClicked : WidgetConfigAction

    data object CloseClicked : WidgetConfigAction
}
