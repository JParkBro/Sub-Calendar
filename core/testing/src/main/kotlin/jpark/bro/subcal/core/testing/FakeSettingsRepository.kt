package jpark.bro.subcal.core.testing

import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.ListSort
import jpark.bro.subcal.core.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository : SettingsRepository {
    val appSettings = MutableStateFlow(AppSettings())
    val notificationSettings = MutableStateFlow(NotificationSettings())
    val listSort = MutableStateFlow(ListSort.NEXT_BILLING)
    val onboardingDone = MutableStateFlow(false)

    override fun observeAppSettings(): Flow<AppSettings> = appSettings

    override suspend fun setAppSettings(settings: AppSettings): Result<Unit> {
        appSettings.value = settings
        return Result.success(Unit)
    }

    override fun observeNotificationSettings(): Flow<NotificationSettings> = notificationSettings

    override suspend fun setNotificationSettings(settings: NotificationSettings): Result<Unit> {
        notificationSettings.value = settings
        return Result.success(Unit)
    }

    override fun observeListSort(): Flow<ListSort> = listSort

    override suspend fun setListSort(sort: ListSort): Result<Unit> {
        listSort.value = sort
        return Result.success(Unit)
    }

    override fun observeOnboardingDone(): Flow<Boolean> = onboardingDone

    override suspend fun setOnboardingDone(done: Boolean): Result<Unit> {
        onboardingDone.value = done
        return Result.success(Unit)
    }
}
