package jpark.bro.subcal.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.subcal.core.database.SubCalDatabase
import jpark.bro.subcal.core.model.WidgetBackground
import jpark.bro.subcal.core.model.WidgetConfig
import jpark.bro.subcal.core.model.WidgetSize
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
class DefaultWidgetConfigRepositoryTest {
    private val db =
        Room
            .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SubCalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    private val repository =
        DefaultWidgetConfigRepository(db.widgetConfigDao(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))

    @After
    fun tearDown() = db.close()

    @Test
    fun `놓은 위젯은 기본값으로 저장되고 이미 있으면 그대로 둔다`() =
        runTest {
            repository.saveConfig(WidgetConfig(7, WidgetSize.SMALL, WidgetBackground.DARK)).getOrThrow()

            repository.saveConfigIfAbsent(WidgetConfig(7, WidgetSize.SMALL)).getOrThrow()
            repository.saveConfigIfAbsent(WidgetConfig(8, WidgetSize.MEDIUM)).getOrThrow()

            assertEquals(
                listOf(
                    WidgetConfig(7, WidgetSize.SMALL, WidgetBackground.DARK),
                    WidgetConfig(8, WidgetSize.MEDIUM, WidgetBackground.THEME),
                ),
                repository.observeConfigs().first(),
            )
        }

    @Test
    fun `지운 위젯의 설정을 지운다`() =
        runTest {
            repository.saveConfig(WidgetConfig(7, WidgetSize.SMALL)).getOrThrow()
            repository.saveConfig(WidgetConfig(8, WidgetSize.MEDIUM)).getOrThrow()

            repository.deleteConfigs(listOf(7)).getOrThrow()

            assertEquals(listOf(8), repository.observeConfigs().first().map { it.widgetId })
        }
}
