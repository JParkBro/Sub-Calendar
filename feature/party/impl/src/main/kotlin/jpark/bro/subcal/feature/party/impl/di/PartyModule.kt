package jpark.bro.subcal.feature.party.impl.di

import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.party.api.PartyKey
import jpark.bro.subcal.feature.party.api.PartyListKey
import jpark.bro.subcal.feature.party.impl.list.PartyListRoot
import jpark.bro.subcal.feature.party.impl.list.PartyListViewModel
import jpark.bro.subcal.feature.party.impl.party.PartyRoot
import jpark.bro.subcal.feature.party.impl.party.PartyViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val partyModule =
    module {
        viewModel { params ->
            PartyViewModel(
                key = params.get(),
                subscriptionRepository = get(),
                partyRepository = get(),
                settingsRepository = get(),
                clock = get(),
            )
        }
        viewModelOf(::PartyListViewModel)

        navigation<PartyKey> { key ->
            val navigator = get<Navigator>()
            PartyRoot(key = key, onBack = navigator::goBack)
        }
        navigation<PartyListKey> {
            val navigator = get<Navigator>()
            PartyListRoot(onOpenParty = { navigator.goTo(PartyKey(it)) }, onBack = navigator::goBack)
        }
    }
