package jpark.bro.subcal.feature.home.impl.di

import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.home.api.HomeKey
import jpark.bro.subcal.feature.home.impl.HomeRoot
import jpark.bro.subcal.feature.home.impl.HomeViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val homeModule =
    module {
        viewModelOf(::HomeViewModel)

        navigation<HomeKey> {
            val navigator = get<Navigator>()
            HomeRoot(onNavigate = navigator::goTo, onSwitchTab = navigator::switchTab)
        }
    }
