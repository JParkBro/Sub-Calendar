package jpark.bro.subcal.feature.onboarding.impl

import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Money

internal data class OnboardingState(
    val isLoading: Boolean = true,
    val query: String = "",
    /** null이면 전체 */
    val category: Category? = null,
    val categories: List<Category> = emptyList(),
    /** 요금제가 있는 프리셋만 (요금이 없으면 한꺼번에 등록할 수 없다) */
    val services: List<OnboardingService> = emptyList(),
    val selectedCount: Int = 0,
    /** 고른 서비스의 한 달 환산 합 (원) */
    val monthlyTotalKrw: Long = 0,
    val isSaving: Boolean = false,
    /** 프리셋을 읽지 못하면 건너뛰기만 할 수 있다 */
    val loadFailed: Boolean = false,
)

internal data class OnboardingService(
    val id: String,
    val name: String,
    val colorKey: String,
    /** 첫 요금제 금액 */
    val amount: Money,
    val isSelected: Boolean,
)
