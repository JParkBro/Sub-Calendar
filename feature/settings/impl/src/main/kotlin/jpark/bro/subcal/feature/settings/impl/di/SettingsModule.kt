package jpark.bro.subcal.feature.settings.impl.di

import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.settings.api.DataReset
import jpark.bro.subcal.feature.settings.api.DataResetHandler
import jpark.bro.subcal.feature.settings.api.MoreKey
import jpark.bro.subcal.feature.settings.api.NotificationSettingsKey
import jpark.bro.subcal.feature.settings.impl.more.MoreRoot
import jpark.bro.subcal.feature.settings.impl.more.MoreViewModel
import jpark.bro.subcal.feature.settings.impl.notification.NotificationSettingsRoot
import jpark.bro.subcal.feature.settings.impl.notification.NotificationSettingsViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val settingsModule =
    module {
        viewModelOf(::MoreViewModel)
        viewModelOf(::NotificationSettingsViewModel)

        navigation<MoreKey> {
            val navigator = get<Navigator>()
            MoreRoot(
                onNavigate = navigator::goTo,
                onDataRestored = { get<DataResetHandler>().handle(DataReset.Restored) },
                onDataDeleted = { get<DataResetHandler>().handle(DataReset.Deleted) },
            )
        }
        navigation<NotificationSettingsKey> {
            val navigator = get<Navigator>()
            NotificationSettingsRoot(onBack = navigator::goBack)
        }
    }
