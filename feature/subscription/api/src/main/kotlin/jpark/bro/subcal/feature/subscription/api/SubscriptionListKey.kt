package jpark.bro.subcal.feature.subscription.api

import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.core.model.Category
import kotlinx.serialization.Serializable

/** 목록 탭. [category]가 있으면 그 카테고리로 필터한다 */
@Serializable
data class SubscriptionListKey(
    val category: Category? = null,
) : NavKey
