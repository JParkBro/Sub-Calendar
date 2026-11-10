package jpark.bro.subcal.core.notification.di

import jpark.bro.subcal.core.notification.PaymentNotifier
import jpark.bro.subcal.core.notification.ReminderScheduler
import jpark.bro.subcal.core.notification.ReminderWorker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** WorkManager는 dataModule이 제공한다 */
val notificationModule =
    module {
        singleOf(::PaymentNotifier)
        singleOf(::ReminderScheduler)
        workerOf(::ReminderWorker)
    }
