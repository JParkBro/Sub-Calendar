package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.ListSort
import jpark.bro.subcal.core.model.NotificationSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeAppSettings(): Flow<AppSettings>

    /** 범위를 벗어난 월 시작일 · 환율이면 실패 */
    suspend fun setAppSettings(settings: AppSettings): Result<Unit>

    fun observeNotificationSettings(): Flow<NotificationSettings>

    /** 고를 수 없는 알림 시점 · 시각이면 실패 */
    suspend fun setNotificationSettings(settings: NotificationSettings): Result<Unit>

    fun observeListSort(): Flow<ListSort>

    suspend fun setListSort(sort: ListSort): Result<Unit>

    fun observeOnboardingDone(): Flow<Boolean>

    suspend fun setOnboardingDone(done: Boolean): Result<Unit>
}
