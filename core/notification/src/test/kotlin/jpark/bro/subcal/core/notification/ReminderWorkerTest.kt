package jpark.bro.subcal.core.notification

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import jpark.bro.subcal.core.datastore.SettingsDataStore
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.SampleData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.io.File
import java.nio.file.Files

@RunWith(AndroidJUnit4::class)
class ReminderWorkerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val today = SampleData.today
    private val subscriptions = FakeSubscriptionRepository()
    private val settingsDataStore =
        SettingsDataStore(
            PreferenceDataStoreFactory.create {
                File(Files.createTempDirectory("settings").toFile(), "settings.preferences_pb")
            },
        )

    @Before
    fun grantPermission() {
        shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    }

    private suspend fun runWorker(): ListenableWorker.Result =
        TestListenableWorkerBuilder<ReminderWorker>(context)
            .setWorkerFactory(
                object : WorkerFactory() {
                    override fun createWorker(
                        appContext: Context,
                        workerClassName: String,
                        workerParameters: WorkerParameters,
                    ): ListenableWorker =
                        ReminderWorker(
                            appContext,
                            workerParameters,
                            subscriptions,
                            settingsDataStore,
                            ReminderNotifier(appContext),
                            SampleData.clock,
                        )
                },
            ).build()
            .doWork()

    private fun postedTitles(): List<String> =
        shadowOf(notificationManager)
            .allNotifications
            .map { it.extras.getCharSequence("android.title").toString() }
            .sorted()

    @Test
    fun `내일 결제마다 알림을 보내고 두 건 이상이면 묶음 요약도 보낸다`() =
        runTest {
            subscriptions.records.value =
                listOf(
                    monthly("넷플릭스", first = today.plusDays(1)),
                    monthly("멜론", first = today.plusDays(1)),
                    monthly("티빙", first = today.plusDays(2)),
                )

            assertEquals(ListenableWorker.Result.success(), runWorker())

            assertEquals(listOf("넷플릭스", "다가오는 결제 2건", "멜론"), postedTitles())
            assertEquals(today, settingsDataStore.lastPaymentReminderDate.first())
        }

    @Test
    fun `체험 종료 전날에는 결제 알림 대신 체험 알림 하나만 보낸다`() =
        runTest {
            subscriptions.records.value =
                listOf(
                    monthly("디즈니+", first = today.plusDays(1), trialStartedOn = today.minusDays(29)),
                    monthly("넷플릭스", first = today.plusDays(1)),
                )

            runWorker()

            assertEquals(listOf("넷플릭스", "디즈니+ 무료체험"), postedTitles())
            val channels =
                shadowOf(notificationManager)
                    .allNotifications
                    .map { it.channelId }
                    .toSet()
            assertEquals(setOf(ReminderNotifier.CHANNEL_PAYMENT, ReminderNotifier.CHANNEL_TRIAL), channels)
        }

    @Test
    fun `한 건이면 요약 없이 그 알림만 보낸다`() =
        runTest {
            subscriptions.records.value = listOf(monthly("넷플릭스", first = today.plusDays(1)))

            runWorker()

            assertEquals(listOf("넷플릭스"), postedTitles())
        }

    @Test
    fun `오늘 이미 보냈으면 다시 보내지 않는다`() =
        runTest {
            subscriptions.records.value = listOf(monthly("넷플릭스", first = today.plusDays(1)))
            settingsDataStore.setLastPaymentReminderDate(today)

            assertEquals(ListenableWorker.Result.success(), runWorker())

            assertEquals(emptyList<String>(), postedTitles())
        }

    @Test
    fun `알림 권한이 없으면 보내지 않고 오늘은 끝낸다`() =
        runTest {
            shadowOf(context as Application).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
            subscriptions.records.value = listOf(monthly("넷플릭스", first = today.plusDays(1)))

            runWorker()

            assertEquals(emptyList<String>(), postedTitles())
            assertEquals(today, settingsDataStore.lastPaymentReminderDate.first())
        }
}
