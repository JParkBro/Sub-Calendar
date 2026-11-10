package jpark.bro.subcal.feature.insight.impl.savings

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.PresetRepository
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.data.repository.UsageRepository
import jpark.bro.subcal.core.model.PresetCatalog
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.ui.MviViewModel
import jpark.bro.subcal.feature.insight.api.SavingsKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class SavingsViewModel(
    key: SavingsKey,
    subscriptionRepository: SubscriptionRepository,
    usageRepository: UsageRepository,
    settingsRepository: SettingsRepository,
    presetRepository: PresetRepository,
    private val clock: Clock,
) : MviViewModel<SavingsState, SavingsAction, SavingsEvent>(SavingsState(tab = key.tab)) {
    private val tab = MutableStateFlow(key.tab)

    /** 연간 결제 비교용 프리셋. 읽지 못하면 비교 카드만 빠진다 */
    private val catalog = MutableStateFlow<PresetCatalog?>(null)

    /** 해지 주소를 찾기 위한 최근 구독 목록 */
    private var records: List<SubscriptionRecord> = emptyList()

    init {
        viewModelScope.launch { catalog.value = presetRepository.getCatalog().getOrNull() }
        combine(
            subscriptionRepository.observeRecords(),
            settingsRepository.observeAppSettings(),
            tab,
            catalog,
            usageRepository.observeUsageLogs(),
        ) { records, settings, tab, catalog, usageLogs ->
            this.records = records
            val state = buildSavingsState(records, usageLogs, catalog, settings, LocalDate.now(clock)).copy(tab = tab)
            setState { state }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: SavingsAction) {
        when (action) {
            SavingsAction.BackClicked -> {
                sendEvent(SavingsEvent.NavigateBack)
            }

            is SavingsAction.TabSelected -> {
                tab.value = action.tab
            }

            is SavingsAction.SubscriptionClicked -> {
                sendEvent(SavingsEvent.OpenDetail(action.subscriptionId))
            }

            is SavingsAction.CancelPageClicked -> {
                val url =
                    records
                        .firstOrNull { it.subscription.id == action.subscriptionId }
                        ?.subscription
                        ?.cancelUrl
                        ?.takeIf { it.isNotBlank() }
                sendEvent(url?.let(SavingsEvent::OpenCancelPage) ?: SavingsEvent.OpenDetail(action.subscriptionId))
            }
        }
    }
}
