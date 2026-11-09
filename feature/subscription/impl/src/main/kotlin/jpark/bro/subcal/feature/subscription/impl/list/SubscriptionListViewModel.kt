package jpark.bro.subcal.feature.subscription.impl.list

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.ui.MviViewModel
import jpark.bro.subcal.feature.subscription.api.SubscriptionListKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class SubscriptionListViewModel(
    key: SubscriptionListKey,
    subscriptionRepository: SubscriptionRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<SubscriptionListState, SubscriptionListAction, SubscriptionListEvent>(
        SubscriptionListState(filter = ListFilter(category = key.category)),
    ) {
    /** 화면에서 바꾸는 필터. 저장한 정렬은 DataStore에서 온다 */
    private val filter = MutableStateFlow(ListFilter(category = key.category))

    init {
        combine(
            subscriptionRepository.observeRecords(),
            settingsRepository.observeListSort(),
            settingsRepository.observeAppSettings(),
            filter,
        ) { records, sort, settings, selected ->
            val content =
                buildListContent(
                    records = records,
                    filter = selected,
                    sort = sort,
                    today = LocalDate.now(clock),
                    usdKrwRate = settings.usdKrwRate,
                )
            setState {
                copy(
                    isLoading = false,
                    filter = selected,
                    sort = sort,
                    content = content,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: SubscriptionListAction) {
        when (action) {
            is SubscriptionListAction.StatusFilterSelected -> {
                filter.update { it.copy(status = action.filter) }
            }

            SubscriptionListAction.CategoryFilterCleared -> {
                filter.update { it.copy(category = null) }
            }

            SubscriptionListAction.SortClicked -> {
                setState { copy(isSortSheetOpen = true) }
            }

            is SubscriptionListAction.SortSelected -> {
                setState { copy(isSortSheetOpen = false) }
                // 저장에 실패하면 정렬이 그대로라 사용자가 다시 고를 수 있어 따로 알리지 않는다
                viewModelScope.launch { settingsRepository.setListSort(action.sort) }
            }

            SubscriptionListAction.SortSheetDismissed -> {
                setState { copy(isSortSheetOpen = false) }
            }

            SubscriptionListAction.AddClicked -> {
                sendEvent(SubscriptionListEvent.OpenServicePicker)
            }
        }
    }
}
