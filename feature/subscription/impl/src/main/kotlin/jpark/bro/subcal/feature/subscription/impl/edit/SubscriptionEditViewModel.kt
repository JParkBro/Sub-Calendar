package jpark.bro.subcal.feature.subscription.impl.edit

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.PresetRepository
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.domain.billing.priceOn
import jpark.bro.subcal.core.model.PriceChangeTiming
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.ui.MviViewModel
import jpark.bro.subcal.feature.subscription.api.SubscriptionEditKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class SubscriptionEditViewModel(
    private val key: SubscriptionEditKey,
    private val subscriptionRepository: SubscriptionRepository,
    private val presetRepository: PresetRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<SubscriptionEditState, SubscriptionEditAction, SubscriptionEditEvent>(
        SubscriptionEditState(isEditMode = key.id != null),
    ) {
    private val today: LocalDate get() = LocalDate.now(clock)

    /** 수정할 구독의 저장 값. 추가면 null */
    private var original: SubscriptionRecord? = null

    /** 다른 구독에서 쓴 결제 수단 (자동 완성) */
    private var knownPaymentMethods: List<String> = emptyList()

    init {
        viewModelScope.launch { load() }
    }

    override fun onAction(action: SubscriptionEditAction) {
        when (action) {
            is SubscriptionEditAction.PriceInput -> update { reduce(action) }
            is SubscriptionEditAction.DetailInput -> update { reduce(action) }
            is SubscriptionEditAction.DateClicked -> setState { copy(datePicker = action.target) }
            is SubscriptionEditAction.DatePicked -> update { withDate(action.date) }
            SubscriptionEditAction.DatePickerDismissed -> setState { copy(datePicker = null) }
            is SubscriptionEditAction.TrialToggled -> update { withTrial(action.enabled, today) }
            SubscriptionEditAction.SaveClicked -> onSaveClicked()
            is SubscriptionEditAction.PriceChangeTimingSelected -> onPriceChangeTimingSelected(action.timing)
            SubscriptionEditAction.DeleteClicked -> setState { copy(sheet = EditSheet.DELETE) }
            SubscriptionEditAction.DeleteConfirmed -> onDeleteConfirmed()
            SubscriptionEditAction.SheetDismissed -> setState { copy(sheet = null) }
            SubscriptionEditAction.CancelClicked -> sendEvent(SubscriptionEditEvent.NavigateBack)
        }
    }

    private suspend fun load() {
        val records = subscriptionRepository.observeRecords().first()
        knownPaymentMethods =
            records
                .mapNotNull {
                    it.subscription.paymentMethod
                        ?.trim()
                        ?.takeIf(String::isNotEmpty)
                }.distinct()
        // 프리셋을 읽지 못해도 직접 입력처럼 쓸 수 있다
        val catalog = presetRepository.getCatalog().getOrNull()
        val id = key.id
        if (id == null) {
            val defaultPaymentMethod = settingsRepository.observeAppSettings().first().defaultPaymentMethod
            val preset = catalog?.services?.firstOrNull { it.id == key.presetId }
            update { newSubscription(key, preset, catalog?.pricesAsOf, defaultPaymentMethod, today) }
            return
        }
        val record = records.firstOrNull { it.subscription.id == id }
        if (record == null) {
            sendEvent(SubscriptionEditEvent.NavigateBack)
            return
        }
        original = record
        val plans =
            catalog
                ?.services
                ?.firstOrNull { it.id == record.subscription.presetId }
                ?.plans
                .orEmpty()
        update { fromRecord(record, plans, today) }
    }

    private fun onSaveClicked() {
        val input = currentState.toInput(original) ?: return
        val record = original
        when {
            record == null -> {
                save { subscriptionRepository.add(input).map { } }
            }

            // 같은 통화로 금액을 바꾸면 적용 시점을 묻는다. 통화가 바뀌면 Repository가 처음부터 적용한다
            input.amount != record.priceOn(today) && input.amount.currency == record.subscription.amount.currency -> {
                setState { copy(sheet = EditSheet.PRICE_CHANGE) }
            }

            else -> {
                save { subscriptionRepository.update(record.subscription.id, input) }
            }
        }
    }

    private fun onPriceChangeTimingSelected(timing: PriceChangeTiming) {
        setState { copy(sheet = null) }
        val record = original ?: return
        val input = currentState.toInput(record) ?: return
        save { subscriptionRepository.update(record.subscription.id, input, timing) }
    }

    private fun onDeleteConfirmed() {
        setState { copy(sheet = null) }
        val id = original?.subscription?.id ?: return
        viewModelScope.launch {
            subscriptionRepository
                .delete(id)
                .onSuccess { sendEvent(SubscriptionEditEvent.Deleted) }
                .onFailure { sendEvent(SubscriptionEditEvent.DeleteFailed) }
        }
    }

    private fun save(write: suspend () -> Result<Unit>) {
        update { copy(isSaving = true) }
        viewModelScope.launch {
            write()
                .onSuccess { sendEvent(SubscriptionEditEvent.Saved(isNew = original == null)) }
                .onFailure {
                    update { copy(isSaving = false) }
                    sendEvent(SubscriptionEditEvent.SaveFailed)
                }
        }
    }

    private fun update(reduce: SubscriptionEditState.() -> SubscriptionEditState) {
        setState { reduce().derived(today, original, knownPaymentMethods) }
    }
}
