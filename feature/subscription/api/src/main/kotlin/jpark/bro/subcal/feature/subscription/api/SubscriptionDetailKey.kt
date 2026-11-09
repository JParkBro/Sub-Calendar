package jpark.bro.subcal.feature.subscription.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 구독 상세 */
@Serializable
data class SubscriptionDetailKey(
    val id: String,
) : NavKey
