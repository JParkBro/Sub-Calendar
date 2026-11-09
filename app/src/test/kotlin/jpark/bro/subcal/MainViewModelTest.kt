package jpark.bro.subcal

import app.cash.turbine.test
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.AppThemeMode
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.feature.home.api.HomeKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingKey
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

    @Test
    fun `온보딩을 마치지 않았으면 첫 화면은 온보딩`() {
        assertEquals(OnboardingKey, MainViewModel(settings).startKey.value)
    }

    @Test
    fun `온보딩을 마쳤으면 첫 화면은 홈`() {
        settings.onboardingDone.value = true

        assertEquals(HomeKey, MainViewModel(settings).startKey.value)
    }
}
