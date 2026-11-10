package jpark.bro.subcal.feature.party.impl.list

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.PartyRepository
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.domain.billing.priceOn
import jpark.bro.subcal.core.domain.party.hasParty
import jpark.bro.subcal.core.domain.party.isSettled
import jpark.bro.subcal.core.domain.party.myShare
import jpark.bro.subcal.core.domain.party.partyBills
import jpark.bro.subcal.core.domain.party.partySize
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import java.time.Clock
import java.time.LocalDate

internal class PartyListViewModel(
    subscriptionRepository: SubscriptionRepository,
    partyRepository: PartyRepository,
    settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<PartyListState, PartyListAction, PartyListEvent>(PartyListState()) {
    init {
        combine(
            subscriptionRepository.observeRecords(),
            partyRepository.observeSettlements(),
            settingsRepository.observeAppSettings(),
        ) { records, settlements, settings ->
            val today = LocalDate.now(clock)
            val items =
                records
                    .filter { it.hasParty }
                    .map { record ->
                        val latest = record.partyBills(today, settings.monthStartDay).firstOrNull()
                        PartyListItem(
                            subscriptionId = record.subscription.id,
                            name = record.subscription.name,
                            colorKey = record.subscription.colorKey,
                            partySize = record.partySize,
                            myShare = record.myShare(record.priceOn(today)),
                            unsettledCount =
                                latest?.let { bill ->
                                    record.partyMembers.count { !settlements.isSettled(it, bill.period) }
                                }
                                    ?: 0,
                        )
                    }.sortedBy { it.name }
            setState { copy(isLoading = false, items = items) }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: PartyListAction) {
        when (action) {
            PartyListAction.BackClicked -> sendEvent(PartyListEvent.NavigateBack)
            is PartyListAction.PartyClicked -> sendEvent(PartyListEvent.OpenParty(action.subscriptionId))
        }
    }
}
