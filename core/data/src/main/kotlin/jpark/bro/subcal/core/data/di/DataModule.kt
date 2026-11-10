package jpark.bro.subcal.core.data.di

import androidx.work.WorkManager
import jpark.bro.subcal.core.common.IdGenerator
import jpark.bro.subcal.core.common.UuidIdGenerator
import jpark.bro.subcal.core.data.backup.DefaultBackupRepository
import jpark.bro.subcal.core.data.cleanup.DeletedSubscriptionCleanupScheduler
import jpark.bro.subcal.core.data.cleanup.DeletedSubscriptionCleanupWorker
import jpark.bro.subcal.core.data.repository.BackupRepository
import jpark.bro.subcal.core.data.repository.DefaultPresetRepository
import jpark.bro.subcal.core.data.repository.DefaultSettingsRepository
import jpark.bro.subcal.core.data.repository.DefaultSubscriptionRepository
import jpark.bro.subcal.core.data.repository.DefaultUsageRepository
import jpark.bro.subcal.core.data.repository.DefaultWidgetConfigRepository
import jpark.bro.subcal.core.data.repository.PresetRepository
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.data.repository.UsageRepository
import jpark.bro.subcal.core.data.repository.WidgetConfigRepository
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.time.Clock

private const val PRESET_ASSET = "presets/services_ko.json"

val dataModule =
    module {
        single<Clock> { Clock.systemDefaultZone() }
        // app이 Koin의 workManagerFactory()로 초기화한 뒤에 꺼낸다
        single { WorkManager.getInstance(androidContext()) }
        singleOf(::DeletedSubscriptionCleanupScheduler)
        workerOf(::DeletedSubscriptionCleanupWorker)
        single<IdGenerator> { UuidIdGenerator }
        singleOf(::DefaultSubscriptionRepository) bind SubscriptionRepository::class
        singleOf(::DefaultUsageRepository) bind UsageRepository::class
        singleOf(::DefaultSettingsRepository) bind SettingsRepository::class
        singleOf(::DefaultBackupRepository) bind BackupRepository::class
        singleOf(::DefaultWidgetConfigRepository) bind WidgetConfigRepository::class
        single<PresetRepository> {
            val context = androidContext()
            DefaultPresetRepository(
                readJson = {
                    context.assets
                        .open(PRESET_ASSET)
                        .bufferedReader()
                        .use { it.readText() }
                },
                ioDispatcher = Dispatchers.IO,
            )
        }
    }
