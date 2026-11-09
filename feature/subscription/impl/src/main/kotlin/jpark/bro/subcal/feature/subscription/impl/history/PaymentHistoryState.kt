package jpark.bro.subcal.feature.subscription.impl.history

import jpark.bro.subcal.core.domain.billing.PaymentHistoryEntry

internal data class PaymentHistoryState(
    val isLoading: Boolean = true,
    val name: String = "",
    /** 첫 결제부터 오늘까지, 최신순 */
    val entries: List<PaymentHistoryEntry> = emptyList(),
)
