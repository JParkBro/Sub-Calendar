package jpark.bro.subcal.core.notification.di

import androidx.work.WorkManager
import jpark.bro.subcal.core.notification.PaymentNotifier
import jpark.bro.subcal.core.notification.ReminderScheduler
import jpark.bro.subcal.core.notification.ReminderWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** WorkManager는 app이 Koin의 workManagerFactory()로 초기화한 뒤에 꺼낸다 */
val notificationModule =
    module {
        single { WorkManager.getInstance(androidContext()) }
        singleOf(::PaymentNotifier)
        singleOf(::ReminderScheduler)
        workerOf(::ReminderWorker)
    }
