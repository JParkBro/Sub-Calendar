package jpark.bro.subcal.feature.subscription.impl.di

import jpark.bro.subcal.feature.subscription.api.SubscriptionListKey
import jpark.bro.subcal.feature.subscription.impl.list.SubscriptionListRoot
import jpark.bro.subcal.feature.subscription.impl.list.SubscriptionListViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val subscriptionModule =
    module {
        viewModel { params ->
            SubscriptionListViewModel(
                key = params.get(),
                subscriptionRepository = get(),
                settingsRepository = get(),
                clock = get(),
            )
        }

        navigation<SubscriptionListKey> { key ->
            SubscriptionListRoot(key = key)
        }
    }
