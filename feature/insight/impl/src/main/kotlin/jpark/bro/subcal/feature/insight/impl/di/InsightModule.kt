package jpark.bro.subcal.feature.insight.impl.di

import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.insight.api.SavingsKey
import jpark.bro.subcal.feature.insight.api.StatsKey
import jpark.bro.subcal.feature.insight.impl.savings.SavingsRoot
import jpark.bro.subcal.feature.insight.impl.savings.SavingsViewModel
import jpark.bro.subcal.feature.insight.impl.stats.StatsRoot
import jpark.bro.subcal.feature.insight.impl.stats.StatsViewModel
import jpark.bro.subcal.feature.insight.impl.value.ValueAnalysisKey
import jpark.bro.subcal.feature.insight.impl.value.ValueAnalysisRoot
import jpark.bro.subcal.feature.insight.impl.value.ValueAnalysisViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val insightModule =
    module {
        viewModelOf(::StatsViewModel)
        viewModelOf(::ValueAnalysisViewModel)
        viewModel { params ->
            SavingsViewModel(
                key = params.get(),
                subscriptionRepository = get(),
                usageRepository = get(),
                settingsRepository = get(),
                presetRepository = get(),
                clock = get(),
            )
        }

        navigation<StatsKey> {
            val navigator = get<Navigator>()
            StatsRoot(onNavigate = navigator::goTo, onSwitchTab = navigator::switchTab)
        }
        navigation<SavingsKey> { key ->
            val navigator = get<Navigator>()
            SavingsRoot(key = key, onNavigate = navigator::goTo, onBack = navigator::goBack)
        }
        navigation<ValueAnalysisKey> {
            val navigator = get<Navigator>()
            ValueAnalysisRoot(onNavigate = navigator::goTo, onBack = navigator::goBack)
        }
    }
