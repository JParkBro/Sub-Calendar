package jpark.bro.subcal.feature.calendar.impl.di

import jpark.bro.subcal.feature.calendar.api.CalendarKey
import jpark.bro.subcal.feature.calendar.impl.CalendarRoot
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val calendarModule =
    module {
        navigation<CalendarKey> {
            CalendarRoot()
        }
    }
