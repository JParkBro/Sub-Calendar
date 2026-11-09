package jpark.bro.subcal.feature.calendar.impl.di

import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.calendar.api.CalendarKey
import jpark.bro.subcal.feature.calendar.impl.CalendarRoot
import jpark.bro.subcal.feature.calendar.impl.CalendarViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val calendarModule =
    module {
        viewModelOf(::CalendarViewModel)

        navigation<CalendarKey> {
            val navigator = get<Navigator>()
            CalendarRoot(onNavigate = navigator::goTo)
        }
    }
