package jpark.bro.subcal.feature.home.impl.di

import jpark.bro.subcal.feature.home.api.HomeKey
import jpark.bro.subcal.feature.home.impl.HomeRoot
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val homeModule =
    module {
        navigation<HomeKey> {
            HomeRoot()
        }
    }
