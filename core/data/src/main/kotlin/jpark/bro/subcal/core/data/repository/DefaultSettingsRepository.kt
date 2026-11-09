package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.common.suspendRunCatching
import jpark.bro.subcal.core.datastore.SettingsDataStore
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.ListSort
import jpark.bro.subcal.core.model.NotificationSettings
import kotlinx.coroutines.flow.Flow

internal class DefaultSettingsRepository(
    private val dataStore: SettingsDataStore,
) : SettingsRepository {
    override fun observeAppSettings(): Flow<AppSettings> = dataStore.appSettings

    override suspend fun setAppSettings(settings: AppSettings): Result<Unit> =
        suspendRunCatching { dataStore.setAppSettings(settings) }

    override fun observeNotificationSettings(): Flow<NotificationSettings> = dataStore.notificationSettings

    override suspend fun setNotificationSettings(settings: NotificationSettings): Result<Unit> =
        suspendRunCatching { dataStore.setNotificationSettings(settings) }

    override fun observeListSort(): Flow<ListSort> = dataStore.listSort

    override suspend fun setListSort(sort: ListSort): Result<Unit> = suspendRunCatching { dataStore.setListSort(sort) }

    override fun observeOnboardingDone(): Flow<Boolean> = dataStore.onboardingDone

    override suspend fun setOnboardingDone(done: Boolean): Result<Unit> =
        suspendRunCatching { dataStore.setOnboardingDone(done) }

    override fun observeBillingDateHintPending(): Flow<Boolean> = dataStore.billingDateHintPending

    override suspend fun setBillingDateHintPending(pending: Boolean): Result<Unit> =
        suspendRunCatching { dataStore.setBillingDateHintPending(pending) }
}
