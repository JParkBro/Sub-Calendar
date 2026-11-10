package jpark.bro.subcal.feature.insight.impl.value

import java.time.LocalDate
import java.time.YearMonth

/**
 * 가성비 분석 (product-spec 4-10). 내 부담액 기준 (파티 구독은 내 몫).
 * 시안의 "AI 지출 다이어트" · 자동 해지는 넣지 않고 "해지 고민함에 담을까요?" 카드만 둔다 (product-spec 11장).
 */
internal data class ValueAnalysisState(
    val isLoading: Boolean = true,
    /** 이번 달 기간 이름이 되는 달 ("10월 기준") */
    val month: YearMonth = YearMonth.from(LocalDate.ofEpochDay(0)),
    /** 사용 안 함 → 1회당 비용 큰 순 */
    val items: List<ValueItem> = emptyList(),
    /** 사용 안 함이고 아직 해지 고민함에 없는 구독 */
    val considerSuggestions: List<ConsiderSuggestion> = emptyList(),
    /** 버튼을 누르고 저장하는 중 */
    val isWorking: Boolean = false,
)

internal data class ValueItem(
    val id: String,
    val name: String,
    val colorKey: String,
    val useCount: Int,
    /** 0회면 null ("사용 안 함") */
    val costPerUseKrw: Long?,
    /** 내 부담액(월 환산). 파티 구독은 내 몫 */
    val monthlyKrw: Long,
    val isParty: Boolean = false,
    /** 막대 길이: 목록 안 최대 1회당 비용 대비. 사용 안 함은 1 */
    val barFraction: Float,
)

internal data class ConsiderSuggestion(
    val id: String,
    val name: String,
    val monthlyKrw: Long,
)
