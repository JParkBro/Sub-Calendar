package jpark.bro.subcal.widget.config

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.data.repository.WidgetConfigRepository
import jpark.bro.subcal.core.model.WidgetConfig
import jpark.bro.subcal.core.model.WidgetSize
import jpark.bro.subcal.core.ui.MviViewModel
import jpark.bro.subcal.widget.buildWidgetDisplay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

/** [widgetId] · [size]는 런처가 준 위젯 id와 그 크기 */
internal class WidgetConfigViewModel(
    widgetId: Int,
    size: WidgetSize,
    subscriptionRepository: SubscriptionRepository,
    settingsRepository: SettingsRepository,
    private val widgetConfigRepository: WidgetConfigRepository,
    clock: Clock,
) : MviViewModel<WidgetConfigState, WidgetConfigAction, WidgetConfigEvent>(
        WidgetConfigState(widgetId, size),
    ) {
    init {
        viewModelScope.launch {
            val records = subscriptionRepository.observeRecords().first()
            val settings = settingsRepository.observeAppSettings().first()
            val saved = widgetConfigRepository.observeConfigs().first().firstOrNull { it.widgetId == widgetId }
            setState {
                copy(
                    isLoading = false,
                    background = saved?.background ?: background,
                    display = buildWidgetDisplay(records, settings, LocalDate.now(clock)),
                )
            }
        }
    }

    override fun onAction(action: WidgetConfigAction) {
        when (action) {
            is WidgetConfigAction.BackgroundSelected -> setState { copy(background = action.background) }
            WidgetConfigAction.SaveClicked -> save()
            WidgetConfigAction.CloseClicked -> sendEvent(WidgetConfigEvent.Close)
        }
    }

    private fun save() {
        val state = currentState
        if (state.isLoading || state.isSaving) return
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            // 저장하면 WidgetUpdater가 위젯 설정 흐름을 보고 다시 그린다
            widgetConfigRepository
                .saveConfig(WidgetConfig(state.widgetId, state.size, state.background))
                .onSuccess { sendEvent(WidgetConfigEvent.Close) }
                .onFailure {
                    setState { copy(isSaving = false) }
                    sendEvent(WidgetConfigEvent.SaveFailed)
                }
        }
    }
}
