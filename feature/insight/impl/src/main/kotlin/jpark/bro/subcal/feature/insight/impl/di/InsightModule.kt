package jpark.bro.subcal.feature.insight.impl.di

import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.insight.api.StatsKey
import jpark.bro.subcal.feature.insight.impl.stats.StatsRoot
import jpark.bro.subcal.feature.insight.impl.stats.StatsViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val insightModule =
    module {
        viewModelOf(::StatsViewModel)

        navigation<StatsKey> {
            val navigator = get<Navigator>()
            StatsRoot(onSwitchTab = navigator::switchTab)
        }
    }
