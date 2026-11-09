package jpark.bro.subcal.feature.subscription.impl.picker

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.preset.matches
import jpark.bro.subcal.core.data.repository.PresetRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Preset
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ServicePickerViewModel(
    private val presetRepository: PresetRepository,
    subscriptionRepository: SubscriptionRepository,
) : MviViewModel<ServicePickerState, ServicePickerAction, ServicePickerEvent>(ServicePickerState()) {
    private val presets = MutableStateFlow<List<Preset>?>(null)
    private val search = MutableStateFlow(Search())

    init {
        viewModelScope.launch {
            presetRepository
                .getCatalog()
                .onSuccess { presets.value = it.services }
                .onFailure {
                    presets.value = emptyList()
                    setState { copy(loadFailed = true) }
                }
        }
        combine(presets, subscriptionRepository.observeRecords(), search) { presets, records, search ->
            if (presets == null) return@combine
            val registered =
                records
                    .filter { it.subscription.status == SubscriptionStatus.ACTIVE }
                    .mapNotNullTo(mutableSetOf()) { it.subscription.presetId }
            setState {
                copy(
                    isLoading = false,
                    query = search.query,
                    category = search.category,
                    categories = presets.map { it.category }.distinct().sorted(),
                    services =
                        presets
                            .filter { it.matches(search.query, search.category) }
                            .map { it.toItem(isRegistered = it.id in registered) },
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: ServicePickerAction) {
        when (action) {
            is ServicePickerAction.QueryChanged -> {
                search.update { it.copy(query = action.query) }
            }

            is ServicePickerAction.CategorySelected -> {
                search.update { it.copy(category = action.category) }
            }

            is ServicePickerAction.ServiceClicked -> {
                sendEvent(ServicePickerEvent.OpenEdit(action.presetId))
            }

            ServicePickerAction.ManualEntryClicked -> {
                sendEvent(ServicePickerEvent.OpenEdit(presetId = null))
            }

            ServicePickerAction.CloseClicked -> {
                sendEvent(ServicePickerEvent.NavigateBack)
            }
        }
    }

    private data class Search(
        val query: String = "",
        val category: Category? = null,
    )
}

private fun Preset.toItem(isRegistered: Boolean): PresetItem =
    PresetItem(
        id = id,
        name = name,
        category = category,
        colorKey = colorKey,
        lowestAmount = plans.minByOrNull { it.amount.amountMinor }?.amount,
        hasMorePlans = plans.size > 1,
        isRegistered = isRegistered,
    )
