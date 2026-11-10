package jpark.bro.subcal.feature.subscription.impl.detail

import jpark.bro.subcal.core.domain.billing.PaymentHistoryEntry
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Money
import java.time.LocalDate

internal data class SubscriptionDetailState(
    val isLoading: Boolean = true,
    /** 구독을 읽은 뒤에만 있다 */
    val content: DetailContent? = null,
    /** "해지했나요?" · "해지로 표시" 시트 */
    val isCancelSheetOpen: Boolean = false,
    /** 해지하러 가기로 브라우저를 열었다. 돌아오면 시트를 띄운다 */
    val isAwaitingCancelReturn: Boolean = false,
    /** 시트에서 고른 해지일 (기본 오늘) */
    val cancelDate: LocalDate = LocalDate.ofEpochDay(0),
    val isCancelDatePickerOpen: Boolean = false,
    /** 오늘. 해지일 선택 상한 */
    val today: LocalDate = LocalDate.ofEpochDay(0),
    /** 일시정지 · 해지 등 저장 중 */
    val isWorking: Boolean = false,
    /** 헤더 메뉴(해지 고민함에 담기 · 빼기) */
    val isMenuOpen: Boolean = false,
    /** 사용 체크 "+1"을 길게 눌러 여는 날짜 선택 */
    val isUsageDatePickerOpen: Boolean = false,
)

internal data class DetailContent(
    val id: String,
    val name: String,
    val planName: String?,
    val colorKey: String,
    val cycle: BillingCycle,
    val firstBillingDate: LocalDate,
    val paymentMethod: String?,
    val status: DetailStatus,
    /** 오늘 유효한 금액 */
    val amount: Money,
    /** 누적 지출 (원) */
    val cumulativeKrw: Long,
    val paymentCount: Int,
    /** 이용 중 · 체험 중일 때만 */
    val nextPayment: NextPayment?,
    /** 최근 결제 [RECENT_PAYMENTS]회와 그 사이 일시정지, 최신순 */
    val recentHistory: List<PaymentHistoryEntry>,
    val hasMoreHistory: Boolean,
    val memo: String?,
    val hasCancelUrl: Boolean,
    /** 해지 고민함에 담았는지 */
    val considerCancel: Boolean = false,
    /** 사용 체크를 켠 이용 중 구독만 (product-spec 4-4) */
    val usage: DetailUsage? = null,
)

/** 사용 체크 카드: "이번 달 n번 썼어요 · 1회당 n원" */
internal data class DetailUsage(
    val count: Int,
    /** 0회면 null */
    val costPerUseKrw: Long?,
)

internal data class NextPayment(
    val date: LocalDate,
    val daysUntil: Int,
)

/** 상태 뱃지와 하단 버튼 (product-spec 4-4) */
internal enum class DetailStatus {
    ACTIVE,

    /** 결과를 고르기 전인 체험 */
    TRIAL,
    PAUSED,
    CANCELLED,
}

/** 상세에 보여주는 최근 결제 횟수 */
internal const val RECENT_PAYMENTS = 12
