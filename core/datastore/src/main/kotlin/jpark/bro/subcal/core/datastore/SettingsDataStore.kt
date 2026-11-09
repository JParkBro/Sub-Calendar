package jpark.bro.subcal.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.ListSort
import jpark.bro.subcal.core.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeParseException

/** 기기에만 저장하는 설정 (data-model-erd 4장). 저장된 값이 범위를 벗어나면 기본값을 쓴다 */
class SettingsDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    /** 파일을 읽지 못하면(IOException) 기본값으로 동작한다. 그 밖의 예외는 그대로 던진다 */
    private val preferences: Flow<Preferences> =
        dataStore.data.catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }

    val appSettings: Flow<AppSettings> =
        preferences.map { prefs ->
            val default = AppSettings()
            AppSettings(
                themeMode = prefs[THEME_MODE].toEnumOr(default.themeMode),
                monthStartDay =
                    prefs[MONTH_START_DAY]?.takeIf { it in AppSettings.MonthStartDays } ?: default.monthStartDay,
                usdKrwRate = prefs[USD_KRW_RATE]?.takeIf { it > 0 } ?: default.usdKrwRate,
                defaultPaymentMethod = prefs[DEFAULT_PAYMENT_METHOD],
            )
        }

    suspend fun setAppSettings(settings: AppSettings) {
        require(settings.monthStartDay in AppSettings.MonthStartDays) { "고를 수 없는 월 시작 기준일입니다." }
        require(settings.usdKrwRate > 0) { "환율은 0보다 커야 합니다." }
        dataStore.edit { prefs ->
            prefs[THEME_MODE] = settings.themeMode.name
            prefs[MONTH_START_DAY] = settings.monthStartDay
            prefs[USD_KRW_RATE] = settings.usdKrwRate
            val paymentMethod = settings.defaultPaymentMethod
            if (paymentMethod ==
                null
            ) {
                prefs.remove(DEFAULT_PAYMENT_METHOD)
            } else {
                prefs[DEFAULT_PAYMENT_METHOD] = paymentMethod
            }
        }
    }

    val notificationSettings: Flow<NotificationSettings> =
        preferences.map { prefs ->
            val default = NotificationSettings()
            NotificationSettings(
                paymentEnabled = prefs[NOTIFY_PAYMENT_ENABLED] ?: default.paymentEnabled,
                defaultDaysBefore =
                    prefs[NOTIFY_DEFAULT_DAYS_BEFORE]?.takeIf { it in NotificationSettings.DaysBeforeOptions }
                        ?: default.defaultDaysBefore,
                trialEnabled = prefs[NOTIFY_TRIAL_ENABLED] ?: default.trialEnabled,
                partyEnabled = prefs[NOTIFY_PARTY_ENABLED] ?: default.partyEnabled,
                monthlySummaryEnabled = prefs[NOTIFY_MONTHLY_SUMMARY_ENABLED] ?: default.monthlySummaryEnabled,
                time = prefs[NOTIFY_TIME].toLocalTimeOrNull()?.takeIf(LocalTime::isSelectable) ?: default.time,
            )
        }

    suspend fun setNotificationSettings(settings: NotificationSettings) {
        require(settings.defaultDaysBefore in NotificationSettings.DaysBeforeOptions) { "고를 수 없는 알림 시점입니다." }
        require(settings.time.isSelectable()) { "고를 수 없는 알림 시각입니다." }
        dataStore.edit { prefs ->
            prefs[NOTIFY_PAYMENT_ENABLED] = settings.paymentEnabled
            prefs[NOTIFY_DEFAULT_DAYS_BEFORE] = settings.defaultDaysBefore
            prefs[NOTIFY_TRIAL_ENABLED] = settings.trialEnabled
            prefs[NOTIFY_PARTY_ENABLED] = settings.partyEnabled
            prefs[NOTIFY_MONTHLY_SUMMARY_ENABLED] = settings.monthlySummaryEnabled
            prefs[NOTIFY_TIME] = settings.time.toString()
        }
    }

    val listSort: Flow<ListSort> = preferences.map { prefs -> prefs[LIST_SORT].toEnumOr(ListSort.NEXT_BILLING) }

    suspend fun setListSort(sort: ListSort) {
        dataStore.edit { prefs -> prefs[LIST_SORT] = sort.name }
    }

    val onboardingDone: Flow<Boolean> = preferences.map { prefs -> prefs[ONBOARDING_DONE] ?: false }

    suspend fun setOnboardingDone(done: Boolean) {
        dataStore.edit { prefs -> prefs[ONBOARDING_DONE] = done }
    }

    /** 온보딩에서 한꺼번에 등록한 뒤 홈에 "결제일을 확인해 주세요" 카드를 보여줄지 (product-spec 5장) */
    val billingDateHintPending: Flow<Boolean> =
        preferences.map { prefs ->
            prefs[BILLING_DATE_HINT_PENDING] ?: false
        }

    suspend fun setBillingDateHintPending(pending: Boolean) {
        dataStore.edit { prefs -> prefs[BILLING_DATE_HINT_PENDING] = pending }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val MONTH_START_DAY = intPreferencesKey("month_start_day")
        val USD_KRW_RATE = longPreferencesKey("usd_krw_rate")
        val DEFAULT_PAYMENT_METHOD = stringPreferencesKey("default_payment_method")
        val NOTIFY_PAYMENT_ENABLED = booleanPreferencesKey("notify_payment_enabled")
        val NOTIFY_DEFAULT_DAYS_BEFORE = intPreferencesKey("notify_default_days_before")
        val NOTIFY_TRIAL_ENABLED = booleanPreferencesKey("notify_trial_enabled")
        val NOTIFY_PARTY_ENABLED = booleanPreferencesKey("notify_party_enabled")
        val NOTIFY_MONTHLY_SUMMARY_ENABLED = booleanPreferencesKey("notify_monthly_summary_enabled")
        val NOTIFY_TIME = stringPreferencesKey("notify_time")
        val LIST_SORT = stringPreferencesKey("list_sort")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val BILLING_DATE_HINT_PENDING = booleanPreferencesKey("billing_date_hint_pending")
    }
}

private inline fun <reified T : Enum<T>> String?.toEnumOr(default: T): T =
    enumValues<T>().firstOrNull { it.name == this } ?: default

private fun String?.toLocalTimeOrNull(): LocalTime? =
    this?.let {
        try {
            LocalTime.parse(it)
        } catch (_: DateTimeParseException) {
            null
        }
    }

private fun LocalTime.isSelectable(): Boolean =
    !isBefore(NotificationSettings.EarliestTime) && !isAfter(NotificationSettings.LatestTime)
