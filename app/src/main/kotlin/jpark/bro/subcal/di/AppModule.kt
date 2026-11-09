package jpark.bro.subcal.di

import jpark.bro.subcal.MainViewModel
import jpark.bro.subcal.core.data.di.dataModule
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.database.di.databaseModule
import jpark.bro.subcal.core.datastore.di.datastoreModule
import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.calendar.impl.di.calendarModule
import jpark.bro.subcal.feature.home.impl.di.homeModule
import jpark.bro.subcal.feature.onboarding.api.OnboardingResultHandler
import jpark.bro.subcal.feature.onboarding.impl.di.onboardingModule
import jpark.bro.subcal.feature.subscription.impl.di.subscriptionModule
import jpark.bro.subcal.navigation.AppOnboardingResultHandler
import jpark.bro.subcal.navigation.TopLevelDestination
import jpark.bro.subcal.navigation.startKeyFor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule =
    module {
        single {
            // 첫 화면을 정하려고 앱 시작 때 한 번만 DataStore를 기다린다 (데이즈와 같은 방식)
            val onboardingDone = runBlocking { get<SettingsRepository>().observeOnboardingDone().first() }
            Navigator(startKey = startKeyFor(onboardingDone), topLevelKeys = TopLevelDestination.keys)
        }
        single<OnboardingResultHandler> { AppOnboardingResultHandler(get()) }
        viewModelOf(::MainViewModel)

        includes(
            // core
            databaseModule,
            datastoreModule,
            dataModule,
            // feature
            onboardingModule,
            homeModule,
            subscriptionModule,
            calendarModule,
        )
    }
