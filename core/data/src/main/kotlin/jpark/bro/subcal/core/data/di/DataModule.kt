package jpark.bro.subcal.core.data.di

import jpark.bro.subcal.core.common.IdGenerator
import jpark.bro.subcal.core.common.UuidIdGenerator
import jpark.bro.subcal.core.data.backup.DefaultBackupRepository
import jpark.bro.subcal.core.data.repository.BackupRepository
import jpark.bro.subcal.core.data.repository.DefaultPresetRepository
import jpark.bro.subcal.core.data.repository.DefaultSettingsRepository
import jpark.bro.subcal.core.data.repository.DefaultSubscriptionRepository
import jpark.bro.subcal.core.data.repository.PresetRepository
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.time.Clock

private const val PRESET_ASSET = "presets/services_ko.json"

val dataModule =
    module {
        single<Clock> { Clock.systemDefaultZone() }
        single<IdGenerator> { UuidIdGenerator }
        singleOf(::DefaultSubscriptionRepository) bind SubscriptionRepository::class
        singleOf(::DefaultSettingsRepository) bind SettingsRepository::class
        singleOf(::DefaultBackupRepository) bind BackupRepository::class
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
