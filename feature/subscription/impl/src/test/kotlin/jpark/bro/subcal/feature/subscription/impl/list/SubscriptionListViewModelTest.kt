package jpark.bro.subcal.feature.subscription.impl.list

import app.cash.turbine.test
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.ListSort
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import jpark.bro.subcal.feature.subscription.api.SubscriptionListKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** product-spec 9장 샘플 (오늘 = 2026.10.30) */
class SubscriptionListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val settings = FakeSettingsRepository()

    private fun viewModel(category: Category? = null) =
        SubscriptionListViewModel(SubscriptionListKey(category), subscriptions, settings, SampleData.clock)

    private val SubscriptionListState.names: List<String>
        get() = content.sections.flatMap { section -> section.rows.map { it.name } }

    @Test
    fun `이용 중은 체험 결과를 고르기 전인 구독을 빼고 월 환산 합을 보여준다`() {
        val state = viewModel().state.value

        assertFalse(state.isLoading)
        assertEquals(StatusFilter.ACTIVE, state.filter.status)
        assertEquals(76_190, state.content.monthlyTotalKrw)
        assertEquals(7, state.names.size)
    }

    @Test
    fun `개수가 0인 상태 칩은 숨긴다`() {
        val chips =
            viewModel()
                .state.value.content.filters

        assertEquals(
            listOf(
                StatusFilterChip(StatusFilter.ACTIVE, 7),
                StatusFilterChip(StatusFilter.CANCELLED, 1),
                StatusFilterChip(StatusFilter.TRIAL, 2),
            ),
            chips,
        )
    }

    @Test
    fun `결제일순은 카테고리로 묶고 묶음 안은 다음 결제일순`() {
        val sections =
            viewModel()
                .state.value.content.sections

        assertEquals(
            listOf(Category.VIDEO, Category.MUSIC, Category.SHOPPING, Category.CLOUD),
            sections.map { it.category },
        )
        assertEquals(listOf("넷플릭스 프리미엄", "유튜브 프리미엄", "티빙"), sections[0].rows.map { it.name })
        assertEquals(listOf("스포티파이", "멜론"), sections[1].rows.map { it.name })
    }

    @Test
    fun `금액순은 월 환산 큰 순으로 묶지 않고 같은 금액은 이름순`() {
        settings.listSort.value = ListSort.AMOUNT
        val state = viewModel().state.value

        assertEquals(1, state.content.sections.size)
        assertNull(
            state.content.sections
                .single()
                .category,
        )
        assertEquals(
            listOf("넷플릭스 프리미엄", "유튜브 프리미엄", "멜론", "스포티파이", "티빙", "쿠팡 와우", "구글 원 200GB"),
            state.names,
        )
    }

    @Test
    fun `이름순은 가나다순`() {
        settings.listSort.value = ListSort.NAME

        assertEquals(
            listOf("구글 원 200GB", "넷플릭스 프리미엄", "멜론", "스포티파이", "유튜브 프리미엄", "쿠팡 와우", "티빙"),
            viewModel().state.value.names,
        )
    }

    @Test
    fun `가격 변경을 반영한 금액과 매년 구독의 월 환산액`() {
        val rows =
            viewModel()
                .state.value.content.sections
                .flatMap { it.rows }
                .associateBy { it.name }

        assertEquals(Money(17_000, Currency.KRW), rows.getValue("넷플릭스 프리미엄").amount)
        assertNull(rows.getValue("넷플릭스 프리미엄").monthlyKrw)
        assertEquals(3_700L, rows.getValue("구글 원 200GB").monthlyKrw)
    }

    @Test
    fun `체험 중 필터를 고르면 체험 구독만`() {
        val viewModel = viewModel()

        viewModel.onAction(SubscriptionListAction.StatusFilterSelected(StatusFilter.TRIAL))

        assertEquals(listOf("디즈니+ 프리미엄", "밀리의 서재"), viewModel.state.value.names)
        assertEquals(StatusFilter.TRIAL, viewModel.state.value.filter.status)
    }

    @Test
    fun `해지 필터의 구독은 아이콘을 흐리게 한다`() {
        val viewModel = viewModel()

        viewModel.onAction(SubscriptionListAction.StatusFilterSelected(StatusFilter.CANCELLED))

        val row =
            viewModel.state.value.content.sections
                .single()
                .rows
                .single()
        assertEquals("왓챠", row.name)
        assertTrue(row.isInactive)
    }

    @Test
    fun `카테고리로 들어오면 그 카테고리만 세고 해제하면 전체`() {
        val viewModel = viewModel(Category.VIDEO)

        assertEquals(Category.VIDEO, viewModel.state.value.filter.category)
        assertEquals(42_800, viewModel.state.value.content.monthlyTotalKrw)
        assertEquals(
            listOf(
                StatusFilterChip(StatusFilter.ACTIVE, 3),
                StatusFilterChip(StatusFilter.CANCELLED, 1),
                StatusFilterChip(StatusFilter.TRIAL, 1),
            ),
            viewModel.state.value.content.filters,
        )

        viewModel.onAction(SubscriptionListAction.CategoryFilterCleared)

        assertNull(viewModel.state.value.filter.category)
        assertEquals(7, viewModel.state.value.names.size)
    }

    @Test
    fun `일시정지하면 일시정지 칩이 생기고 목록에서 빠진다`() {
        val viewModel = viewModel()

        subscriptions.records.value =
            SampleData.records.map {
                if (it == SampleData.tving) {
                    it.copy(subscription = it.subscription.copy(status = SubscriptionStatus.PAUSED))
                } else {
                    it
                }
            }

        val content = viewModel.state.value.content
        assertTrue(StatusFilterChip(StatusFilter.PAUSED, 1) in content.filters)
        assertFalse("티빙" in viewModel.state.value.names)
    }

    @Test
    fun `정렬을 고르면 시트를 닫고 저장한다`() {
        val viewModel = viewModel()

        viewModel.onAction(SubscriptionListAction.SortClicked)
        assertTrue(viewModel.state.value.isSortSheetOpen)

        viewModel.onAction(SubscriptionListAction.SortSelected(ListSort.NAME))

        assertFalse(viewModel.state.value.isSortSheetOpen)
        assertEquals(ListSort.NAME, settings.listSort.value)
        assertEquals(ListSort.NAME, viewModel.state.value.sort)
    }

    @Test
    fun `구독이 없으면 묶음이 비어 있다`() {
        subscriptions.records.value = emptyList()
        val state = viewModel().state.value

        assertFalse(state.isLoading)
        assertTrue(state.content.sections.isEmpty())
        assertEquals(0, state.content.monthlyTotalKrw)
    }

    @Test
    fun `추가 버튼은 서비스 선택을 연다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.event.test {
                viewModel.onAction(SubscriptionListAction.AddClicked)
                assertEquals(SubscriptionListEvent.OpenServicePicker, awaitItem())
            }
        }

    @Test
    fun `행을 누르면 상세를 연다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.event.test {
                viewModel.onAction(SubscriptionListAction.RowClicked("netflix"))
                assertEquals(SubscriptionListEvent.OpenDetail("netflix"), awaitItem())
            }
        }
}
