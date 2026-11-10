package jpark.bro.subcal.feature.settings.impl.widget

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.HomeWidgets
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class WidgetSettingsViewModel(
    private val homeWidgets: HomeWidgets,
) : MviViewModel<WidgetSettingsState, WidgetSettingsAction, WidgetSettingsEvent>(WidgetSettingsState()) {
    init {
        homeWidgets
            .observePlacedWidgets()
            .onEach { widgets ->
                setState { copy(isLoading = false, widgets = widgets, canAddWidget = homeWidgets.isPinSupported()) }
            }.launchIn(viewModelScope)
    }

    override fun onAction(action: WidgetSettingsAction) {
        when (action) {
            WidgetSettingsAction.BackClicked -> {
                sendEvent(WidgetSettingsEvent.NavigateBack)
            }

            // 설정 화면은 위젯 모듈의 Activity라 HomeWidgets가 연다
            is WidgetSettingsAction.WidgetClicked -> {
                homeWidgets.openConfigure(action.widgetId)
            }

            WidgetSettingsAction.AddClicked -> {
                setState { copy(isSizeSheetOpen = true) }
            }

            is WidgetSettingsAction.SizeSelected -> {
                setState { copy(isSizeSheetOpen = false) }
                if (!homeWidgets.requestPin(action.size)) sendEvent(WidgetSettingsEvent.AddFailed)
            }

            WidgetSettingsAction.SizeSheetDismissed -> {
                setState { copy(isSizeSheetOpen = false) }
            }
        }
    }
}
