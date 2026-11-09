package jpark.bro.subcal.feature.subscription.impl.di

import jpark.bro.subcal.feature.subscription.api.SubscriptionListKey
import jpark.bro.subcal.feature.subscription.impl.SubscriptionListRoot
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val subscriptionModule =
    module {
        navigation<SubscriptionListKey> {
            SubscriptionListRoot()
        }
    }
