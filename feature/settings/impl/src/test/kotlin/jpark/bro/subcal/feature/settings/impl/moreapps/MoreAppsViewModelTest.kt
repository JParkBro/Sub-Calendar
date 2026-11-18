package jpark.bro.subcal.feature.settings.impl.moreapps

import app.cash.turbine.test
import jpark.bro.subcal.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MoreAppsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel = MoreAppsViewModel()
    private val state get() = viewModel.state.value

    @Test
    fun `처음에는 로딩 중이다`() {
        assertEquals(MoreAppsState(isLoading = true, isFailed = false, reloadCount = 0), state)
    }

    @Test
    fun `페이지를 불러오면 로딩을 끝낸다`() {
        viewModel.onAction(MoreAppsAction.PageLoaded)

        assertEquals(MoreAppsState(isLoading = false, isFailed = false), state)
    }

    @Test
    fun `페이지를 불러오지 못하면 실패를 보여준다`() {
        viewModel.onAction(MoreAppsAction.PageFailed)

        assertEquals(MoreAppsState(isLoading = false, isFailed = true), state)
    }

    @Test
    fun `다시 시도하면 로딩으로 돌아가고 다시 불러오기 카운터를 올린다`() {
        viewModel.onAction(MoreAppsAction.PageFailed)

        viewModel.onAction(MoreAppsAction.RetryClicked)
        assertEquals(MoreAppsState(isLoading = true, isFailed = false, reloadCount = 1), state)

        viewModel.onAction(MoreAppsAction.PageFailed)
        viewModel.onAction(MoreAppsAction.RetryClicked)
        assertEquals(2, state.reloadCount)
    }

    @Test
    fun `스토어 링크를 누르면 스토어를 연다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(MoreAppsAction.StoreLinkClicked("market://details?id=jpark.bro.days"))

                assertEquals(MoreAppsEvent.OpenStore("market://details?id=jpark.bro.days"), awaitItem())
            }
        }

    @Test
    fun `뒤로를 누르면 이전 화면으로 간다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(MoreAppsAction.BackClicked)

                assertEquals(MoreAppsEvent.NavigateBack, awaitItem())
            }
        }
}
