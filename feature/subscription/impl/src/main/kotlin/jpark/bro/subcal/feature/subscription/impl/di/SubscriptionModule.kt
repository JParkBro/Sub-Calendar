package jpark.bro.subcal.feature.subscription.impl.di

import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.party.api.PartyKey
import jpark.bro.subcal.feature.subscription.api.ServicePickerKey
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey
import jpark.bro.subcal.feature.subscription.api.SubscriptionEditKey
import jpark.bro.subcal.feature.subscription.api.SubscriptionListKey
import jpark.bro.subcal.feature.subscription.impl.detail.SubscriptionDetailRoot
import jpark.bro.subcal.feature.subscription.impl.detail.SubscriptionDetailViewModel
import jpark.bro.subcal.feature.subscription.impl.edit.SubscriptionEditRoot
import jpark.bro.subcal.feature.subscription.impl.edit.SubscriptionEditViewModel
import jpark.bro.subcal.feature.subscription.impl.history.PaymentHistoryKey
import jpark.bro.subcal.feature.subscription.impl.history.PaymentHistoryRoot
import jpark.bro.subcal.feature.subscription.impl.history.PaymentHistoryViewModel
import jpark.bro.subcal.feature.subscription.impl.list.SubscriptionListRoot
import jpark.bro.subcal.feature.subscription.impl.list.SubscriptionListViewModel
import jpark.bro.subcal.feature.subscription.impl.picker.ServicePickerRoot
import jpark.bro.subcal.feature.subscription.impl.picker.ServicePickerViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val subscriptionModule =
    module {
        viewModel { params ->
            SubscriptionListViewModel(
                key = params.get(),
                subscriptionRepository = get(),
                settingsRepository = get(),
                clock = get(),
            )
        }
        viewModelOf(::ServicePickerViewModel)
        viewModel { params ->
            SubscriptionEditViewModel(
                key = params.get(),
                subscriptionRepository = get(),
                presetRepository = get(),
                settingsRepository = get(),
                clock = get(),
            )
        }

        viewModel { params ->
            SubscriptionDetailViewModel(
                key = params.get(),
                subscriptionRepository = get(),
                usageRepository = get(),
                settingsRepository = get(),
                clock = get(),
            )
        }
        viewModel { params ->
            PaymentHistoryViewModel(id = params.get(), subscriptionRepository = get(), clock = get())
        }

        navigation<SubscriptionListKey> { key ->
            val navigator = get<Navigator>()
            SubscriptionListRoot(
                key = key,
                onAddClick = { navigator.goTo(ServicePickerKey) },
                onOpenDetail = { id -> navigator.goTo(SubscriptionDetailKey(id)) },
            )
        }
        navigation<SubscriptionDetailKey> { key ->
            val navigator = get<Navigator>()
            SubscriptionDetailRoot(
                key = key,
                onOpenEdit = { id -> navigator.goTo(SubscriptionEditKey(id = id)) },
                onOpenHistory = { id -> navigator.goTo(PaymentHistoryKey(id)) },
                onOpenParty = { id -> navigator.goTo(PartyKey(id)) },
                onBack = navigator::goBack,
            )
        }
        navigation<PaymentHistoryKey> { key ->
            val navigator = get<Navigator>()
            PaymentHistoryRoot(key = key, onBack = navigator::goBack)
        }
        navigation<ServicePickerKey> {
            val navigator = get<Navigator>()
            ServicePickerRoot(
                onOpenEdit = { presetId -> navigator.goTo(SubscriptionEditKey(presetId = presetId)) },
                onBack = navigator::goBack,
            )
        }
        navigation<SubscriptionEditKey> { key ->
            val navigator = get<Navigator>()
            SubscriptionEditRoot(
                key = key,
                onSaved = {
                    // 서비스 선택을 거쳐 왔으면 선택 화면도 닫고 처음 화면으로 돌아간다
                    navigator.goBack()
                    if (navigator.backStack.lastOrNull() == ServicePickerKey) navigator.goBack()
                },
                onBack = navigator::goBack,
            )
        }
    }
