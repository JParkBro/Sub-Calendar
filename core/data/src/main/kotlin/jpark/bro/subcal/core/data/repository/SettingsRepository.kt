package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.ListSort
import jpark.bro.subcal.core.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

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

    /** 온보딩 등록 후 홈의 "결제일을 확인해 주세요" 카드 */
    fun observeBillingDateHintPending(): Flow<Boolean>

    suspend fun setBillingDateHintPending(pending: Boolean): Result<Unit>

    /** 인앱 업데이트 새 버전 안내에 마지막으로 답한 날. 같은 날에는 다시 묻지 않는다 (in-app-update.md) */
    fun observeUpdatePromptAnsweredOn(): Flow<LocalDate?>

    suspend fun setUpdatePromptAnsweredOn(date: LocalDate): Result<Unit>
}
