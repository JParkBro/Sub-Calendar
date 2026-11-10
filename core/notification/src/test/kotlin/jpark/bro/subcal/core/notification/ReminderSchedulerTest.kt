package jpark.bro.subcal.core.notification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.SampleData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class ReminderSchedulerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var workManager: WorkManager
    private lateinit var scheduler: ReminderScheduler

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        workManager = WorkManager.getInstance(context)
        scheduler =
            ReminderScheduler(workManager, FakeSettingsRepository(), ReminderNotifier(context), SampleData.clock)
    }

    private fun scheduled(): List<WorkInfo> =
        workManager
            .getWorkInfosForUniqueWork(ReminderScheduler.WORK_NAME)
            .get()
            .filter { !it.state.isFinished }

    @Test
    fun `같은 시각으로 다시 예약하면 기존 작업을 그대로 둔다`() =
        runTest {
            scheduler.schedule(LocalTime.of(9, 0))
            val first = scheduled().single().id

            scheduler.schedule(LocalTime.of(9, 0))

            assertEquals(first, scheduled().single().id)
        }

    @Test
    fun `시각이 바뀌면 새 시각으로 다시 예약한다`() =
        runTest {
            scheduler.schedule(LocalTime.of(9, 0))
            val first = scheduled().single().id

            scheduler.schedule(LocalTime.of(20, 0))

            val current = scheduled().single()
            assertNotEquals(first, current.id)
            assertTrue(ReminderScheduler.timeTag(LocalTime.of(20, 0)) in current.tags)
        }
}
