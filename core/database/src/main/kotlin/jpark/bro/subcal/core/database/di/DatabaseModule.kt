package jpark.bro.subcal.core.database.di

import androidx.room.Room
import jpark.bro.subcal.core.database.SubCalDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule =
    module {
        single {
            Room.databaseBuilder(androidContext(), SubCalDatabase::class.java, "subcal.db").build()
        }
        single { get<SubCalDatabase>().subscriptionDao() }
        single { get<SubCalDatabase>().dataDao() }
        single { get<SubCalDatabase>().widgetConfigDao() }
    }
