package jpark.bro.subcal.feature.settings.impl.widget

import app.cash.turbine.test
import jpark.bro.subcal.core.model.WidgetConfig
import jpark.bro.subcal.core.model.WidgetSize
import jpark.bro.subcal.core.testing.FakeHomeWidgets
import jpark.bro.subcal.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WidgetSettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val homeWidgets = FakeHomeWidgets(placed = listOf(WidgetConfig(3, WidgetSize.SMALL)))
    private val viewModel by lazy { WidgetSettingsViewModel(homeWidgets) }

    @Test
    fun `놓인 위젯을 보여주고 누르면 그 위젯 설정을 연다`() {
        assertEquals(listOf(WidgetConfig(3, WidgetSize.SMALL)), viewModel.state.value.widgets)
        assertTrue(viewModel.state.value.canAddWidget)

        viewModel.onAction(WidgetSettingsAction.WidgetClicked(3))

        assertEquals(3, homeWidgets.openedWidgetId)
    }

    @Test
    fun `위젯 추가는 크기를 고르면 요청한다`() {
        viewModel.onAction(WidgetSettingsAction.AddClicked)
        assertTrue(viewModel.state.value.isSizeSheetOpen)

        viewModel.onAction(WidgetSettingsAction.SizeSelected(WidgetSize.MEDIUM))

        assertFalse(viewModel.state.value.isSizeSheetOpen)
        assertEquals(WidgetSize.MEDIUM, homeWidgets.requestedSize)
    }

    @Test
    fun `런처가 추가 요청을 받지 못하면 알린다`() =
        runTest {
            homeWidgets.pinAccepted = false

            viewModel.event.test {
                viewModel.onAction(WidgetSettingsAction.SizeSelected(WidgetSize.SMALL))

                assertEquals(WidgetSettingsEvent.AddFailed, awaitItem())
            }
        }

    @Test
    fun `런처가 추가 요청을 지원하지 않으면 버튼을 숨긴다`() {
        homeWidgets.pinSupported = false

        assertFalse(viewModel.state.value.canAddWidget)
    }
}
