package jpark.bro.subcal.feature.trial.impl

import jpark.bro.subcal.core.model.Money
import java.time.LocalDate

/** 무료체험 (product-spec 4-7) */
internal data class TrialState(
    val isLoading: Boolean = true,
    /** 진행 중, 종료 가까운 순 */
    val active: List<ActiveTrial> = emptyList(),
    /** 최근 90일 안에 끝난 체험, 최근 순 */
    val ended: List<EndedTrial> = emptyList(),
    /** 결과를 저장하는 중: 버튼을 막는다 */
    val isWorking: Boolean = false,
)

internal data class ActiveTrial(
    val id: String,
    val name: String,
    val colorKey: String,
    /** 체험 종료일 = 첫 결제일 */
    val firstBillingDate: LocalDate,
    /** 첫 결제 금액 */
    val amount: Money,
    val daysUntil: Int,
    val progress: Float,
    /** 체험 기간 전체 일수 · 지난 일수 */
    val totalDays: Int,
    val elapsedDays: Int,
) {
    /** 종료 1일 이내: danger 테두리 */
    val isUrgent: Boolean get() = daysUntil <= 1
}

internal data class EndedTrial(
    val id: String,
    val name: String,
    val colorKey: String,
    val endedOn: LocalDate,
    /** true면 "해지함 · n원 아낌", false면 "구독 전환" */
    val cancelled: Boolean,
    val savedKrw: Long,
)
