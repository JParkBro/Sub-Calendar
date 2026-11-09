package jpark.bro.subcal.feature.subscription.impl.history

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.domain.billing.paymentHistory
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import java.time.LocalDate

/** 표시 전용 화면이라 Action · Event가 없다. 구독이 삭제되면 상세가 함께 닫힌다 */
internal class PaymentHistoryViewModel(
    id: String,
    subscriptionRepository: SubscriptionRepository,
    clock: Clock,
) : MviViewModel<PaymentHistoryState, Nothing, Nothing>(PaymentHistoryState()) {
    init {
        subscriptionRepository
            .observeRecord(id)
            .filterNotNull()
            .onEach { record ->
                setState {
                    copy(
                        isLoading = false,
                        name = record.subscription.name,
                        entries = record.paymentHistory(LocalDate.now(clock)),
                    )
                }
            }.launchIn(viewModelScope)
    }

    override fun onAction(action: Nothing) = Unit
}
