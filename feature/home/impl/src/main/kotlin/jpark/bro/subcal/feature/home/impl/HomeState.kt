package jpark.bro.subcal.feature.home.impl

import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Payment
import java.time.LocalDate

internal data class HomeState(
    val isLoading: Boolean = true,
    /** 등록한 구독이 하나도 없으면 빈 상태 + 빠른 등록 칩 */
    val isEmpty: Boolean = false,
    /** 온보딩에서 한꺼번에 등록한 뒤 "결제일을 확인해 주세요" (product-spec 5장) */
    val showBillingDateHint: Boolean = false,
    val summary: MonthSummary = MonthSummary(),
    val upcoming: List<UpcomingPayment> = emptyList(),
    val categories: List<CategoryCount> = emptyList(),
)

/** 이번 달 카드 (product-spec 4-1 · 2-8). 금액은 모두 원 */
internal data class MonthSummary(
    /** 기간 이름이 되는 달 (1~12) */
    val month: Int = 1,
    /** 월 시작 기준일이 1이 아닐 때만: 기간 첫날 ~ 마지막 날 */
    val range: ClosedRange<LocalDate>? = null,
    val totalKrw: Long = 0,
    /** 이번 달 − 지난달 */
    val changeKrw: Long = 0,
    val monthlyAverageKrw: Long = 0,
    val annualKrw: Long = 0,
)

internal data class UpcomingPayment(
    val payment: Payment,
    val daysUntil: Int,
)

internal data class CategoryCount(
    val category: Category,
    val count: Int,
)

/** 빈 상태의 빠른 등록 칩. [presetId]가 null이면 직접 입력 */
internal enum class QuickAdd(
    val presetId: String?,
) {
    NETFLIX("netflix"),
    YOUTUBE_PREMIUM("youtube_premium"),
    COUPANG_WOW("coupang_wow"),
    MANUAL(null),
}

/** 다가오는 결제 개수 (4-1) */
internal const val UPCOMING_LIMIT = 3
