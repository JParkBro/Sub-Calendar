package jpark.bro.subcal.feature.onboarding.impl.di

import jpark.bro.subcal.feature.onboarding.api.OnboardingKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingResult
import jpark.bro.subcal.feature.onboarding.api.OnboardingResultHandler
import jpark.bro.subcal.feature.onboarding.impl.OnboardingRoot
import jpark.bro.subcal.feature.onboarding.impl.OnboardingViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val onboardingModule =
    module {
        viewModelOf(::OnboardingViewModel)

        navigation<OnboardingKey> {
            OnboardingRoot(onFinished = { get<OnboardingResultHandler>().handle(OnboardingResult.Done) })
        }
    }
