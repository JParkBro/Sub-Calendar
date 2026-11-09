package jpark.bro.subcal.feature.subscription.impl.picker

import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Money

internal data class ServicePickerState(
    val isLoading: Boolean = true,
    val query: String = "",
    /** null이면 전체 */
    val category: Category? = null,
    /** 프리셋에 있는 카테고리 (칩) */
    val categories: List<Category> = emptyList(),
    val services: List<PresetItem> = emptyList(),
    /** 프리셋을 읽지 못하면 직접 입력만 할 수 있다 */
    val loadFailed: Boolean = false,
)

internal data class PresetItem(
    val id: String,
    val name: String,
    val category: Category,
    val colorKey: String,
    /** 가장 싼 요금제 금액. 요금제가 없으면 null */
    val lowestAmount: Money?,
    /** 요금제가 2개 이상이면 금액 뒤에 "~" */
    val hasMorePlans: Boolean,
    /** 이용 중인 구독이 이미 있다 (다시 등록은 가능) */
    val isRegistered: Boolean,
)
