package jpark.bro.subcal.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.subcal.core.database.entity.PauseEntity
import jpark.bro.subcal.core.database.entity.PriceChangeEntity
import jpark.bro.subcal.core.database.entity.SubscriptionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubscriptionDaoTest {
    private lateinit var db: SubCalDatabase
    private val dao get() = db.subscriptionDao()

    @Before
    fun createDb() {
        db =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SubCalDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun closeDb() = db.close()

    private fun subscription(
        id: String,
        createdAt: Long = 1,
    ) = SubscriptionEntity(
        id = id,
        presetId = null,
        name = id,
        planName = null,
        category = "ETC",
        amountMinor = 10_000,
        currency = "KRW",
        cycleUnit = "MONTH",
        cycleInterval = 1,
        firstBillingDate = "2026-01-01",
        trialStartedOn = null,
        trialOutcome = "NONE",
        status = "ACTIVE",
        cancelledOn = null,
        paymentMethod = null,
        reminderMode = "DEFAULT",
        reminderDaysBefore = null,
        colorKey = "BLUE",
        cancelUrl = null,
        memo = null,
        considerCancel = false,
        trackUsage = false,
        partySplitMode = "EQUAL",
        createdAt = createdAt,
        updatedAt = createdAt,
        deletedAt = null,
    )

    private fun priceChange(
        id: String,
        effectiveFrom: String = "2026-02-01",
    ) = PriceChangeEntity(id, "a", effectiveFrom, 12_000, 1)

    private fun pause(id: String) = PauseEntity(id, "a", "2026-02-01", null, 1)

    @Test
    fun `등록순으로 나오고 삭제한 구독은 빠진다`() =
        runTest {
            dao.upsertSubscription(subscription("b", createdAt = 2))
            dao.upsertSubscription(subscription("a", createdAt = 1))
            dao.upsertSubscription(subscription("c", createdAt = 3))

            dao.softDelete("c", deletedAt = 9)

            assertEquals(listOf("a", "b"), dao.observeAll().first().map { it.subscription.id })
            assertNull(dao.get("c"))
            assertNull(dao.observe("c").first())
        }

    @Test
    fun `soft delete는 행을 남기고 시각을 기록한다`() =
        runTest {
            dao.upsertSubscription(subscription("a"))

            dao.softDelete("a", deletedAt = 9)

            db.query("SELECT deleted_at, updated_at FROM subscription WHERE id = 'a'", null).use {
                assertTrue(it.moveToFirst())
                assertEquals(9, it.getLong(0))
                assertEquals(9, it.getLong(1))
            }
        }

    @Test
    fun `구독과 함께 가격 변경 · 일시정지를 읽는다`() =
        runTest {
            dao.upsertSubscription(subscription("a"))
            dao.upsertPriceChange(priceChange("p"))
            dao.upsertPause(pause("q"))

            val row = dao.get("a")!!

            assertEquals(listOf("p"), row.priceChanges.map { it.id })
            assertEquals(listOf("q"), row.pauses.map { it.id })
        }

    @Test
    fun `같은 구독의 같은 적용일 가격 변경은 하나만 허용`() =
        runTest {
            dao.upsertSubscription(subscription("a"))
            dao.upsertPriceChange(priceChange("p"))

            dao.upsertPriceChange(priceChange("other"))

            assertEquals(listOf("p"), dao.get("a")!!.priceChanges.map { it.id })
        }

    @Test
    fun `저장에서 기존 가격 변경을 지울 수 있다`() =
        runTest {
            dao.upsertSubscription(subscription("a"))
            dao.upsertPriceChange(priceChange("p"))

            dao.save(subscription("a"), clearPriceChanges = true)

            assertEquals(emptyList<String>(), dao.get("a")!!.priceChanges.map { it.id })
        }

    @Test
    fun `일시정지 행을 저장하거나 지운다`() =
        runTest {
            dao.upsertSubscription(subscription("a"))

            dao.save(subscription("a"), pauseToSave = pause("q"))
            assertEquals(listOf("q"), dao.get("a")!!.pauses.map { it.id })

            dao.save(subscription("a"), pauseIdToDelete = "q")
            assertEquals(emptyList<String>(), dao.get("a")!!.pauses.map { it.id })
        }

    @Test
    fun `저장해도 하위 행이 지워지지 않는다`() =
        runTest {
            dao.upsertSubscription(subscription("a"))
            dao.upsertPriceChange(priceChange("p"))

            dao.upsertSubscription(subscription("a").copy(name = "수정"))

            assertEquals(listOf("p"), dao.get("a")!!.priceChanges.map { it.id })
        }
}
