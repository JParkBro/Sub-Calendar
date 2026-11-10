package jpark.bro.subcal.core.data.backup

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.subcal.core.common.IdGenerator
import jpark.bro.subcal.core.data.repository.DefaultSubscriptionRepository
import jpark.bro.subcal.core.data.repository.DefaultUsageRepository
import jpark.bro.subcal.core.database.SubCalDatabase
import jpark.bro.subcal.core.database.entity.PartyMemberEntity
import jpark.bro.subcal.core.database.entity.PartySettlementEntity
import jpark.bro.subcal.core.database.entity.UsageLogEntity
import jpark.bro.subcal.core.datastore.SettingsDataStore
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.AppThemeMode
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.model.PriceChangeTiming
import jpark.bro.subcal.core.model.SubscriptionInput
import jpark.bro.subcal.core.model.SubscriptionRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.file.Files
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** 오늘 = 2026.10.30 (product-spec 9장) */
@RunWith(AndroidJUnit4::class)
class DefaultBackupRepositoryTest {
    private val zone: ZoneId = ZoneId.of("Asia/Seoul")
    private val clock =
        Clock.fixed(
            LocalDate
                .of(2026, 10, 30)
                .atTime(12, 0)
                .atZone(zone)
                .toInstant(),
            zone,
        )
    private val databases = mutableListOf<SubCalDatabase>()

    /** 기기 하나: DB · 설정 · 구독 · 백업 Repository */
    private inner class Device {
        val db =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SubCalDatabase::class.java)
                .allowMainThreadQueries()
                .build()
                .also(databases::add)
        val settings =
            SettingsDataStore(
                PreferenceDataStoreFactory.create {
                    File(Files.createTempDirectory("settings").toFile(), "settings.preferences_pb")
                },
            )
        private var nextId = 0
        private val ids = IdGenerator { "id-${++nextId}" }
        val subscriptions = DefaultSubscriptionRepository(db.subscriptionDao(), ids, clock)
        val usage = DefaultUsageRepository(db.usageLogDao(), db.subscriptionDao(), ids, clock)
        val backup = DefaultBackupRepository(db.subscriptionDao(), db.dataDao(), settings, clock)

        /** 사용 기록 · 파티 멤버 · 정산 (내보내기와 같은 기준) */
        suspend fun activity(): List<List<Any>> =
            listOf(db.dataDao().getUsageLogs(), db.dataDao().getPartyMembers(), db.dataDao().getPartySettlements())

        suspend fun records(): List<SubscriptionRecord> = subscriptions.observeRecords().first()
    }

    @After
    fun tearDown() = databases.forEach(SubCalDatabase::close)

    private fun input(
        name: String,
        amount: Long = 10_000,
    ) = SubscriptionInput(
        name = name,
        category = Category.VIDEO,
        amount = Money(amount, Currency.KRW),
        cycle = BillingCycle(CycleUnit.MONTH),
        firstBillingDate = LocalDate.of(2026, 1, 15),
        paymentMethod = "신한카드",
        memo = "가족 계정",
        colorKey = "RED",
    )

    /** 가격 변경 · 일시정지 · 사용 기록 · 파티 · 삭제한 구독과 바꾼 설정이 있는 기기 */
    private suspend fun filledDevice(): Device =
        Device().apply {
            val netflix = subscriptions.add(input("넷플릭스", amount = 13_500)).getOrThrow()
            subscriptions.update(netflix, input("넷플릭스", amount = 17_000), PriceChangeTiming.NEXT_PAYMENT).getOrThrow()
            usage.add(netflix, LocalDate.of(2026, 10, 3)).getOrThrow()
            usage.add(netflix, LocalDate.of(2026, 10, 3)).getOrThrow()
            usage.remove(usage.add(netflix, LocalDate.of(2026, 10, 30)).getOrThrow()).getOrThrow()
            val tving = subscriptions.add(input("티빙")).getOrThrow()
            subscriptions.pause(tving).getOrThrow()
            db.dataDao().insertAll(
                subscriptions = emptyList(),
                priceChanges = emptyList(),
                pauses = emptyList(),
                usageLogs = emptyList(),
                partyMembers =
                    listOf(
                        PartyMemberEntity("민지", netflix, "민지", null, 0, 1, 1, null),
                        PartyMemberEntity("준호", netflix, "준호", 4_000, 1, 1, 2, null),
                        PartyMemberEntity("나간 멤버", netflix, "나간 멤버", null, 2, 1, 3, deletedAt = 3),
                    ),
                partySettlements =
                    listOf(PartySettlementEntity("민지", "2026-10", 5), PartySettlementEntity("나간 멤버", "2026-09", 6)),
            )
            val wrong = subscriptions.add(input("잘못 등록")).getOrThrow()
            usage.add(wrong, LocalDate.of(2026, 10, 1)).getOrThrow()
            subscriptions.delete(wrong).getOrThrow()
            settings.setAppSettings(AppSettings(themeMode = AppThemeMode.DARK, monthStartDay = 25, usdKrwRate = 1_385))
            settings.setNotificationSettings(NotificationSettings(defaultDaysBefore = 3, time = LocalTime.of(20, 0)))
        }

    @Test
    fun `내보내고 다른 기기에서 불러오면 구독 · 가격 변경 · 일시정지 · 설정이 같다`() =
        runTest {
            val source = filledDevice()
            val json = source.backup.export(appVersion = "1.0.0").getOrThrow()

            val target = Device()
            target.subscriptions.add(input("원래 있던 구독")).getOrThrow()
            target.backup.restore(json).getOrThrow()

            assertEquals(source.records(), target.records())
            assertEquals(source.activity(), target.activity())
            assertEquals(source.settings.appSettings.first(), target.settings.appSettings.first())
            assertEquals(source.settings.notificationSettings.first(), target.settings.notificationSettings.first())
        }

    @Test
    fun `삭제한 구독은 내보내지 않고 미리보기에 구독 수와 만든 시각을 보여준다`() =
        runTest {
            val json = filledDevice().backup.export(appVersion = "1.0.0").getOrThrow()

            val summary = Device().backup.inspect(json).getOrThrow()

            assertEquals(2, summary.subscriptionCount)
            assertEquals(clock.millis(), summary.exportedAt)
            assertTrue(!json.contains("잘못 등록"))
            assertTrue(json.contains("\"format\":\"subcal-backup\""))
        }

    @Test
    fun `지운 사용 기록 · 멤버와 지운 구독의 기록은 내보내지 않는다`() =
        runTest {
            val target = Device()
            target.backup.restore(filledDevice().backup.export(appVersion = "1.0.0").getOrThrow()).getOrThrow()

            val (usageLogs, members, settlements) = target.activity()
            assertEquals(listOf("2026-10-03", "2026-10-03"), usageLogs.map { (it as UsageLogEntity).usedOn })
            assertEquals(listOf("민지", "준호"), members.map { (it as PartyMemberEntity).name })
            assertEquals(listOf(PartySettlementEntity("민지", "2026-10", 5)), settlements)
            assertEquals(0, target.db.dataDao().purgeDeletedBefore(Long.MAX_VALUE))
        }

    @Test
    fun `3차 배열이 없는 예전 v1 파일도 불러온다`() =
        runTest {
            val json =
                filledDevice()
                    .backup
                    .export(appVersion = "1.0.0")
                    .getOrThrow()
                    .replace(Regex(",\"usageLogs\":\\[.*$"), "}")
            assertFalse(json.contains("usageLogs"))

            val target = Device()
            target.backup.restore(json).getOrThrow()

            assertEquals(2, target.records().size)
            assertEquals(listOf(emptyList<Any>(), emptyList(), emptyList()), target.activity())
        }

    @Test
    fun `없는 멤버를 가리키는 정산이 있으면 거부한다`() =
        runTest {
            val json = filledDevice().backup.export(appVersion = "1.0.0").getOrThrow()

            val orphan = json.replace("\"partyMemberId\":\"민지\"", "\"partyMemberId\":\"없는 멤버\"")

            assertTrue(Device().backup.restore(orphan).isFailure)
        }

    @Test
    fun `형식 · 버전 · 내용이 잘못된 파일은 거부하고 아무것도 바꾸지 않는다`() =
        runTest {
            val json = filledDevice().backup.export(appVersion = "1.0.0").getOrThrow()
            val target = Device()
            target.subscriptions.add(input("원래 있던 구독")).getOrThrow()
            val before = target.records()

            val broken =
                listOf(
                    "not json",
                    "{}",
                    json.replace("\"subcal-backup\"", "\"other-app\""),
                    json.replace("\"version\":1", "\"version\":2"),
                    json.replace("\"MONTH\"", "\"FORTNIGHT\""),
                    json.replace("\"2026-01-15\"", "\"2026-13-45\""),
                    json.replace("\"monthStartDay\":25", "\"monthStartDay\":31"),
                )
            broken.forEach { file ->
                assertTrue(file.take(30), target.backup.inspect(file).isFailure)
                assertTrue(file.take(30), target.backup.restore(file).isFailure)
            }

            assertEquals(before, target.records())
            assertEquals(AppSettings(), target.settings.appSettings.first())
        }

    @Test
    fun `모든 데이터를 지우면 삭제한 구독까지 사라지고 설정 · 온보딩이 처음 상태로 돌아간다`() =
        runTest {
            val device = filledDevice()
            device.settings.setOnboardingDone(true)

            device.backup.deleteAll().getOrThrow()

            assertTrue(device.records().isEmpty())
            assertEquals(0, device.db.dataDao().purgeDeletedBefore(Long.MAX_VALUE))
            assertEquals(AppSettings(), device.settings.appSettings.first())
            assertEquals(NotificationSettings(), device.settings.notificationSettings.first())
            assertTrue(!device.settings.onboardingDone.first())
        }

    @Test
    fun `없는 구독을 가리키는 일시정지가 있으면 거부한다`() =
        runTest {
            val source = filledDevice()
            val json = source.backup.export(appVersion = "1.0.0").getOrThrow()
            val tvingId =
                source
                    .records()
                    .first { it.subscription.name == "티빙" }
                    .subscription.id

            val orphan = json.replace("\"subscriptionId\":\"$tvingId\"", "\"subscriptionId\":\"없는-구독\"")

            assertTrue(Device().backup.restore(orphan).isFailure)
        }
}
