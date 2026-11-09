package jpark.bro.subcal.feature.onboarding.impl

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.preset.matches
import jpark.bro.subcal.core.data.repository.PresetRepository
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.domain.billing.monthlyEquivalentKrw
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Preset
import jpark.bro.subcal.core.model.SubscriptionInput
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class OnboardingViewModel(
    private val presetRepository: PresetRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<OnboardingState, OnboardingAction, OnboardingEvent>(OnboardingState()) {
    private val today: LocalDate get() = LocalDate.now(clock)

    /** 요금제가 있는 프리셋. 읽기 전에는 null */
    private val presets = MutableStateFlow<List<Preset>?>(null)
    private val ui = MutableStateFlow(Ui())

    /** 이미 저장한 프리셋. 저장 중 실패하고 다시 누르면 건너뛴다 */
    private val registered = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            presetRepository
                .getCatalog()
                .onSuccess { catalog -> presets.value = catalog.services.filter { it.plans.isNotEmpty() } }
                .onFailure {
                    presets.value = emptyList()
                    setState { copy(loadFailed = true) }
                }
        }
        combine(presets, ui, settingsRepository.observeAppSettings()) { presets, ui, settings ->
            if (presets == null) return@combine
            val selected = presets.filter { it.id in ui.selected }
            setState {
                copy(
                    isLoading = false,
                    query = ui.query,
                    category = ui.category,
                    categories = presets.map { it.category }.distinct().sorted(),
                    services =
                        presets
                            .filter { it.matches(ui.query, ui.category) }
                            .map { it.toService(isSelected = it.id in ui.selected) },
                    selectedCount = selected.size,
                    monthlyTotalKrw = selected.sumOf { it.monthlyKrw(settings.usdKrwRate) },
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.QueryChanged -> {
                ui.update { it.copy(query = action.query) }
            }

            is OnboardingAction.CategorySelected -> {
                ui.update { it.copy(category = action.category) }
            }

            // 이미 저장한 서비스는 선택을 풀어도 지워지지 않으니 풀 수 없게 한다
            is OnboardingAction.ServiceToggled -> {
                if (action.presetId !in
                    registered
                ) {
                    ui.update { it.toggle(action.presetId) }
                }
            }

            OnboardingAction.RegisterClicked -> {
                register()
            }

            // 저장 중 실패한 뒤 건너뛰어도 이미 저장한 구독은 결제일 확인이 필요하다
            OnboardingAction.SkipClicked -> {
                finish(showBillingDateHint = registered.isNotEmpty())
            }
        }
    }

    /** 첫 요금제 · 요금제 주기 · 첫 결제일 오늘로 한꺼번에 저장 (product-spec 5장) */
    private fun register() {
        val chosen = presets.value.orEmpty().filter { it.id in ui.value.selected && it.id !in registered }
        if (ui.value.selected.isEmpty() || currentState.isSaving) return
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            val failed =
                chosen.any { preset ->
                    subscriptionRepository
                        .add(preset.toInput(today))
                        .onSuccess { registered += preset.id }
                        .isFailure
                }
            if (failed) {
                setState { copy(isSaving = false) }
                sendEvent(OnboardingEvent.SaveFailed)
            } else {
                finish(showBillingDateHint = true)
            }
        }
    }

    private fun finish(showBillingDateHint: Boolean) {
        viewModelScope.launch {
            // 안내 카드는 보조 기능이라 저장에 실패해도 진행한다
            if (showBillingDateHint) settingsRepository.setBillingDateHintPending(true)
            settingsRepository
                .setOnboardingDone(true)
                .onSuccess { sendEvent(OnboardingEvent.Finished) }
                .onFailure {
                    setState { copy(isSaving = false) }
                    sendEvent(OnboardingEvent.SaveFailed)
                }
        }
    }

    private data class Ui(
        val query: String = "",
        val category: Category? = null,
        /** 고른 순서대로 */
        val selected: List<String> = emptyList(),
    ) {
        fun toggle(id: String): Ui = copy(selected = if (id in selected) selected - id else selected + id)
    }
}

private fun Preset.toService(isSelected: Boolean): OnboardingService =
    OnboardingService(id = id, name = name, colorKey = colorKey, amount = plans.first().amount, isSelected = isSelected)

private fun Preset.toInput(today: LocalDate): SubscriptionInput {
    val plan = plans.first()
    return SubscriptionInput(
        presetId = id,
        name = name,
        planName = plan.name,
        category = category,
        amount = plan.amount,
        cycle = plan.cycle,
        firstBillingDate = today,
        colorKey = colorKey,
        cancelUrl = cancelUrl,
    )
}

/** 첫 요금제의 한 달 환산액. 계산은 core:domain 규칙을 쓴다 */
private fun Preset.monthlyKrw(usdKrwRate: Long): Long =
    plans.first().let { monthlyEquivalentKrw(it.amount, it.cycle, usdKrwRate) }
