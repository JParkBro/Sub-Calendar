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
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.testing.FakePartyRepository
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
import java.time.Clock
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ReminderWorkerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val today = SampleData.today
    private val subscriptions = FakeSubscriptionRepository()
    private val party = FakePartyRepository(subscriptions)
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

    private suspend fun runWorker(clock: Clock = SampleData.clock): ListenableWorker.Result =
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
                            party,
                            settingsDataStore,
                            ReminderNotifier(appContext),
                            clock,
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
    fun `월초 요약을 켜 두면 이번 달 기간 첫날에 이번 달 구독료를 알린다`() =
        runTest {
            settingsDataStore.setNotificationSettings(NotificationSettings(monthlySummaryEnabled = true))
            subscriptions.records.value = listOf(monthly("넷플릭스", first = LocalDate.of(2026, 1, 15)))
            val firstDay = LocalDate.of(2026, 11, 1)
            val clock =
                Clock.fixed(
                    firstDay.atTime(9, 0).atZone(SampleData.clock.zone).toInstant(),
                    SampleData.clock.zone,
                )

            runWorker(clock)

            assertEquals(listOf("11월 구독료"), postedTitles())
        }

    @Test
    fun `결제 다음 날 미정산 멤버가 있으면 정산 알림을 보낸다`() =
        runTest {
            // 유튜브 프리미엄 11.14 결제, 11월 정산은 아무도 하지 않음
            subscriptions.records.value = listOf(SampleData.youtubePremium)
            party.settlements.value = SampleData.partySettlements
            val dayAfter = LocalDate.of(2026, 11, 15)
            val clock =
                Clock.fixed(
                    dayAfter.atTime(9, 0).atZone(SampleData.clock.zone).toInstant(),
                    SampleData.clock.zone,
                )

            runWorker(clock)

            val posted = shadowOf(notificationManager).allNotifications.single()
            assertEquals("유튜브 프리미엄 정산", posted.extras.getCharSequence("android.title").toString())
            assertEquals("민지 님 외 2명이 아직 보내지 않았어요", posted.extras.getCharSequence("android.text").toString())
            assertEquals(ReminderNotifier.CHANNEL_PARTY, posted.channelId)
        }

    @Test
    fun `정산 알림을 끄면 보내지 않는다`() =
        runTest {
            settingsDataStore.setNotificationSettings(NotificationSettings(partyEnabled = false))
            subscriptions.records.value = listOf(SampleData.youtubePremium)
            val dayAfter = LocalDate.of(2026, 11, 15)
            val clock =
                Clock.fixed(
                    dayAfter.atTime(9, 0).atZone(SampleData.clock.zone).toInstant(),
                    SampleData.clock.zone,
                )

            runWorker(clock)

            assertEquals(emptyList<String>(), postedTitles())
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
