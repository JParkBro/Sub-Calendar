package jpark.bro.subcal.core.notification.di

import jpark.bro.subcal.core.notification.ReminderNotifier
import jpark.bro.subcal.core.notification.ReminderScheduler
import jpark.bro.subcal.core.notification.ReminderWorker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** WorkManager는 dataModule이 제공한다 */
val notificationModule =
    module {
        singleOf(::ReminderNotifier)
        singleOf(::ReminderScheduler)
        workerOf(::ReminderWorker)
    }
