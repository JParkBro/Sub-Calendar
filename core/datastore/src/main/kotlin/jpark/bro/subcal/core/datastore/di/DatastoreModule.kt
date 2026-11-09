package jpark.bro.subcal.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import jpark.bro.subcal.core.datastore.SettingsDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val datastoreModule =
    module {
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.create { androidContext().preferencesDataStoreFile("settings") }
        }
        singleOf(::SettingsDataStore)
    }
