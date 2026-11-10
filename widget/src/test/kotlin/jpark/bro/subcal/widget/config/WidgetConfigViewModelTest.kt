package jpark.bro.subcal.widget.config

import app.cash.turbine.test
import jpark.bro.subcal.core.model.WidgetBackground
import jpark.bro.subcal.core.model.WidgetConfig
import jpark.bro.subcal.core.model.WidgetSize
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.FakeWidgetConfigRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class WidgetConfigViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val configs = FakeWidgetConfigRepository(listOf(WidgetConfig(7, WidgetSize.MEDIUM, WidgetBackground.DARK)))

    private fun viewModel(widgetId: Int = 7) =
        WidgetConfigViewModel(
            widgetId,
            WidgetSize.MEDIUM,
            FakeSubscriptionRepository(SampleData.records),
            FakeSettingsRepository(),
            configs,
            SampleData.clock,
        )

    @Test
    fun `저장된 배경과 위젯과 같은 미리보기 값으로 시작한다`() {
        val state = viewModel().state.value

        assertFalse(state.isLoading)
        assertEquals(WidgetBackground.DARK, state.background)
        assertEquals(72_490L, state.display?.totalKrw)
        assertEquals(WidgetBackground.THEME, viewModel(widgetId = 8).state.value.background)
    }

    @Test
    fun `배경을 바꿔 저장하면 설정을 저장하고 닫는다`() =
        runTest {
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(WidgetConfigAction.BackgroundSelected(WidgetBackground.WHITE))
                viewModel.onAction(WidgetConfigAction.SaveClicked)

                assertEquals(WidgetConfigEvent.Close, awaitItem())
            }
            assertEquals(listOf(WidgetConfig(7, WidgetSize.MEDIUM, WidgetBackground.WHITE)), configs.configs.value)
        }

    @Test
    fun `저장에 실패하면 알리고 다시 저장할 수 있다`() =
        runTest {
            configs.failure = IOException("db")
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(WidgetConfigAction.SaveClicked)

                assertEquals(WidgetConfigEvent.SaveFailed, awaitItem())
            }
            assertFalse(viewModel.state.value.isSaving)
        }
}
