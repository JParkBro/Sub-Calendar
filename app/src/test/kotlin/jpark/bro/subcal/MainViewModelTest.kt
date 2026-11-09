package jpark.bro.subcal

import app.cash.turbine.test
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.AppThemeMode
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settings = FakeSettingsRepository()

    @Test
    fun `화면 모드 설정을 따른다`() =
        runTest {
            val viewModel = MainViewModel(settings)

            viewModel.themeMode.test {
                assertEquals(AppThemeMode.SYSTEM, awaitItem())

                settings.setAppSettings(AppSettings(themeMode = AppThemeMode.DARK))

                assertEquals(AppThemeMode.DARK, awaitItem())
            }
        }
}
