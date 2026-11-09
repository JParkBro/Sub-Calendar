package jpark.bro.subcal.di

import jpark.bro.subcal.MainViewModel
import jpark.bro.subcal.core.data.di.dataModule
import jpark.bro.subcal.core.database.di.databaseModule
import jpark.bro.subcal.core.datastore.di.datastoreModule
import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.calendar.impl.di.calendarModule
import jpark.bro.subcal.feature.home.api.HomeKey
import jpark.bro.subcal.feature.home.impl.di.homeModule
import jpark.bro.subcal.feature.subscription.impl.di.subscriptionModule
import jpark.bro.subcal.navigation.TopLevelDestination
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule =
    module {
        single { Navigator(startKey = HomeKey, topLevelKeys = TopLevelDestination.keys) }
        viewModelOf(::MainViewModel)

        includes(
            // core
            databaseModule,
            datastoreModule,
            dataModule,
            // feature
            homeModule,
            subscriptionModule,
            calendarModule,
        )
    }
