package jpark.bro.subcal.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

/** 구독 한 건과 결제 계산에 필요한 하위 행 */
data class SubscriptionWithRules(
    @Embedded val subscription: SubscriptionEntity,
    @Relation(parentColumn = "id", entityColumn = "subscription_id")
    val priceChanges: List<PriceChangeEntity>,
    @Relation(parentColumn = "id", entityColumn = "subscription_id")
    val pauses: List<PauseEntity>,
)
