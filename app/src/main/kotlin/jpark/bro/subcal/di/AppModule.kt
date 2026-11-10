package jpark.bro.subcal.di

import jpark.bro.subcal.MainViewModel
import jpark.bro.subcal.core.data.di.dataModule
import jpark.bro.subcal.core.database.di.databaseModule
import jpark.bro.subcal.core.datastore.di.datastoreModule
import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.core.notification.di.notificationModule
import jpark.bro.subcal.feature.calendar.impl.di.calendarModule
import jpark.bro.subcal.feature.home.impl.di.homeModule
import jpark.bro.subcal.feature.insight.impl.di.insightModule
import jpark.bro.subcal.feature.onboarding.api.OnboardingResultHandler
import jpark.bro.subcal.feature.onboarding.impl.di.onboardingModule
import jpark.bro.subcal.feature.settings.api.DataResetHandler
import jpark.bro.subcal.feature.settings.impl.di.settingsModule
import jpark.bro.subcal.feature.subscription.impl.di.subscriptionModule
import jpark.bro.subcal.feature.trial.impl.di.trialModule
import jpark.bro.subcal.navigation.AppDataResetHandler
import jpark.bro.subcal.navigation.AppOnboardingResultHandler
import jpark.bro.subcal.navigation.TopLevelDestination
import jpark.bro.subcal.widget.di.widgetModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule =
    module {
        // 첫 화면은 MainViewModel이 DataStore를 읽은 뒤 parametersOf로 넘긴다 (메인 스레드를 막지 않는다)
        single { params -> Navigator(startKey = params.get(), topLevelKeys = TopLevelDestination.keys) }
        single<OnboardingResultHandler> { AppOnboardingResultHandler(get()) }
        single<DataResetHandler> { AppDataResetHandler(get()) }
        viewModelOf(::MainViewModel)

        includes(
            // core
            databaseModule,
            datastoreModule,
            dataModule,
            notificationModule,
            widgetModule,
            // feature
            onboardingModule,
            homeModule,
            subscriptionModule,
            calendarModule,
            insightModule,
            trialModule,
            settingsModule,
        )
    }
