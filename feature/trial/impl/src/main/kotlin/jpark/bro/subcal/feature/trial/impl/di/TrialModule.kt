package jpark.bro.subcal.feature.trial.impl.di

import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.trial.api.TrialKey
import jpark.bro.subcal.feature.trial.impl.TrialRoot
import jpark.bro.subcal.feature.trial.impl.TrialViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val trialModule =
    module {
        viewModelOf(::TrialViewModel)

        navigation<TrialKey> {
            val navigator = get<Navigator>()
            TrialRoot(onNavigate = navigator::goTo, onBack = navigator::goBack)
        }
    }
