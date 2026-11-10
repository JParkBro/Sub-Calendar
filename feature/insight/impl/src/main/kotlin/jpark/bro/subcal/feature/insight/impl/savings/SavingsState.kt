package jpark.bro.subcal.feature.insight.impl.savings

import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.feature.insight.api.SavingsTab
import java.time.LocalDate

/**
 * 아낀 돈 · 해지 고민 (product-spec 4-8). 이번 달 사용 횟수 이유는 사용 체크(3차)와 함께 붙인다.
 * 시안의 "목표 절약률"은 문서에 없는 기능이라 넣지 않는다 (product-spec 11장).
 */
internal data class SavingsState(
    val isLoading: Boolean = true,
    val tab: SavingsTab = SavingsTab.CONSIDER,
    /** 해지 고민함에 담은 이용 중 구독, 월 환산이 큰 순 */
    val considerItems: List<ConsiderItem> = emptyList(),
    /** "아래 n개를 해지하면 연 n원" */
    val considerAnnualKrw: Long = 0,
    val duplicates: List<DuplicateItem> = emptyList(),
    val annualOptions: List<AnnualOptionItem> = emptyList(),
    val savedTotalKrw: Long = 0,
    val savedItems: List<SavedListItem> = emptyList(),
)

internal data class ConsiderItem(
    val id: String,
    val name: String,
    val colorKey: String,
    val monthlyKrw: Long,
    /** 같은 카테고리에서 함께 쓰는 다른 구독 (중복이 아니면 비어 있음) */
    val duplicateWith: List<String>,
)

internal data class DuplicateItem(
    val category: Category,
    val names: List<String>,
    val monthlyKrw: Long,
)

internal data class AnnualOptionItem(
    val id: String,
    val name: String,
    val monthlyAmount: Money,
    val annualAmount: Money,
    val yearlySavingKrw: Long,
)

internal data class SavedListItem(
    val id: String,
    val name: String,
    val colorKey: String,
    val cancelledOn: LocalDate,
    val savedKrw: Long,
)
