package jpark.bro.subcal.feature.subscription.impl.history

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 결제 이력 전체 (상세의 "더보기"). 다른 기능에서 들어오지 않아 impl 안에 둔다 */
@Serializable
internal data class PaymentHistoryKey(
    val id: String,
) : NavKey
