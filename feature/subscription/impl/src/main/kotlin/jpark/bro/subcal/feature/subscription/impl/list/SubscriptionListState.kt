package jpark.bro.subcal.feature.subscription.impl.list

import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.ListSort
import jpark.bro.subcal.core.model.Money
import java.time.LocalDate

internal data class SubscriptionListState(
    val isLoading: Boolean = true,
    val filter: ListFilter = ListFilter(),
    val sort: ListSort = ListSort.NEXT_BILLING,
    val content: SubscriptionListContent = SubscriptionListContent(),
    val isSortSheetOpen: Boolean = false,
)

/** 화면에서 고르는 필터. [category]는 홈 · 통계에서 들어올 때만 있다 */
internal data class ListFilter(
    val status: StatusFilter = StatusFilter.ACTIVE,
    val category: Category? = null,
)

/** 상태 필터 (product-spec 4-2). 체험 중은 결과를 고르기 전인 이용 중 구독이다 */
internal enum class StatusFilter {
    ACTIVE,
    PAUSED,
    CANCELLED,
    TRIAL,
}

internal data class SubscriptionListContent(
    /** 보여줄 상태 칩과 개수. 이용 중과 선택한 칩 외에는 0개면 숨긴다 */
    val filters: List<StatusFilterChip> = listOf(StatusFilterChip(StatusFilter.ACTIVE, 0)),
    /** 현재 필터의 월 환산 합 (원) */
    val monthlyTotalKrw: Long = 0,
    val sections: List<SubscriptionSection> = emptyList(),
)

internal data class StatusFilterChip(
    val filter: StatusFilter,
    val count: Int,
)

/** 결제일순이면 카테고리별 묶음, 금액순 · 이름순이면 [category]가 null인 묶음 하나 */
internal data class SubscriptionSection(
    val category: Category?,
    val rows: List<SubscriptionRow>,
)

internal data class SubscriptionRow(
    val id: String,
    val name: String,
    val planName: String?,
    val colorKey: String,
    val cycle: BillingCycle,
    val firstBillingDate: LocalDate,
    /** 오늘 유효한 금액 */
    val amount: Money,
    /** 매월 · 매주가 아닌 주기만: 한 달 환산액 (원) */
    val monthlyKrw: Long?,
    /** 일시정지 · 해지는 아이콘을 흐리게 */
    val isInactive: Boolean,
    /** 파티 인원 (나 포함). 파티가 아니면 null */
    val partySize: Int? = null,
)
