package jpark.bro.subcal.feature.subscription.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 구독 입력 · 수정. [id]가 있으면 수정, 없으면 추가.
 * 추가할 때 [presetId]가 있으면 프리셋 값을 채우고 [planIndex] 요금제를 고른다. [startAsTrial]이면 무료체험을 켠다.
 */
@Serializable
data class SubscriptionEditKey(
    val id: String? = null,
    val presetId: String? = null,
    val planIndex: Int? = null,
    val startAsTrial: Boolean = false,
) : NavKey
