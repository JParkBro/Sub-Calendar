package jpark.bro.subcal.core.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import jpark.bro.subcal.core.datastore.SettingsDataStore
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.AppThemeMode
import jpark.bro.subcal.core.model.ListSort
import jpark.bro.subcal.core.model.NotificationSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.time.LocalTime

class DefaultSettingsRepositoryTest {
    private val repository =
        DefaultSettingsRepository(
            SettingsDataStore(
                PreferenceDataStoreFactory.create {
                    File(Files.createTempDirectory("settings").toFile(), "settings.preferences_pb")
                },
            ),
        )

    @Test
    fun `기본값은 문서와 같다`() =
        runTest {
            assertEquals(
                AppSettings(AppThemeMode.SYSTEM, monthStartDay = 1, usdKrwRate = 1_400, defaultPaymentMethod = null),
                repository.observeAppSettings().first(),
            )
            assertEquals(
                NotificationSettings(
                    paymentEnabled = true,
                    defaultDaysBefore = 1,
                    trialEnabled = true,
                    partyEnabled = true,
                    monthlySummaryEnabled = false,
                    time = LocalTime.of(9, 0),
                ),
                repository.observeNotificationSettings().first(),
            )
            assertEquals(ListSort.NEXT_BILLING, repository.observeListSort().first())
            assertFalse(repository.observeOnboardingDone().first())
        }

    @Test
    fun `앱 설정을 저장하면 그대로 나온다`() =
        runTest {
            val settings =
                AppSettings(AppThemeMode.DARK, monthStartDay = 25, usdKrwRate = 1_350, defaultPaymentMethod = "신한카드")

            repository.setAppSettings(settings).getOrThrow()

            assertEquals(settings, repository.observeAppSettings().first())
        }

    @Test
    fun `기본 결제 수단을 지울 수 있다`() =
        runTest {
            repository.setAppSettings(AppSettings(defaultPaymentMethod = "신한카드")).getOrThrow()

            repository.setAppSettings(AppSettings(defaultPaymentMethod = null)).getOrThrow()

            assertEquals(null, repository.observeAppSettings().first().defaultPaymentMethod)
        }

    @Test
    fun `월 시작일은 1부터 28까지, 환율은 0보다 커야 한다`() =
        runTest {
            assertTrue(repository.setAppSettings(AppSettings(monthStartDay = 28)).isSuccess)
            assertTrue(repository.setAppSettings(AppSettings(monthStartDay = 29)).isFailure)
            assertTrue(repository.setAppSettings(AppSettings(monthStartDay = 0)).isFailure)
            assertTrue(repository.setAppSettings(AppSettings(usdKrwRate = 0)).isFailure)

            assertEquals(28, repository.observeAppSettings().first().monthStartDay)
        }

    @Test
    fun `알림 설정을 저장하면 그대로 나온다`() =
        runTest {
            val settings =
                NotificationSettings(
                    paymentEnabled = false,
                    defaultDaysBefore = 7,
                    trialEnabled = false,
                    partyEnabled = false,
                    monthlySummaryEnabled = true,
                    time = LocalTime.of(21, 0),
                )

            repository.setNotificationSettings(settings).getOrThrow()

            assertEquals(settings, repository.observeNotificationSettings().first())
        }

    @Test
    fun `알림 시각은 오전 8시부터 오후 9시까지, 시점은 정해진 값만`() =
        runTest {
            assertTrue(repository.setNotificationSettings(NotificationSettings(time = LocalTime.of(8, 0))).isSuccess)
            assertTrue(repository.setNotificationSettings(NotificationSettings(time = LocalTime.of(7, 59))).isFailure)
            assertTrue(repository.setNotificationSettings(NotificationSettings(time = LocalTime.of(21, 1))).isFailure)
            assertTrue(repository.setNotificationSettings(NotificationSettings(defaultDaysBefore = 2)).isFailure)
        }

    @Test
    fun `정렬과 온보딩 완료를 기억한다`() =
        runTest {
            repository.setListSort(ListSort.AMOUNT).getOrThrow()
            repository.setOnboardingDone(true).getOrThrow()

            assertEquals(ListSort.AMOUNT, repository.observeListSort().first())
            assertTrue(repository.observeOnboardingDone().first())
        }
}
