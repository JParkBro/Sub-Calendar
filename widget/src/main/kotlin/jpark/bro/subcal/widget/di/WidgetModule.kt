package jpark.bro.subcal.widget.di

import jpark.bro.subcal.core.data.repository.HomeWidgets
import jpark.bro.subcal.widget.DefaultHomeWidgets
import jpark.bro.subcal.widget.WIDGET_SCOPE
import jpark.bro.subcal.widget.WidgetUpdater
import jpark.bro.subcal.widget.config.WidgetConfigViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val widgetModule =
    module {
        // 위젯 갱신 · 리시버 비동기 작업용. 앱 프로세스와 수명이 같다
        single<CoroutineScope>(named(WIDGET_SCOPE)) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
        single<HomeWidgets> { DefaultHomeWidgets(androidContext(), get()) }
        viewModel { params ->
            WidgetConfigViewModel(
                widgetId = params.get(),
                size = params.get(),
                subscriptionRepository = get(),
                settingsRepository = get(),
                widgetConfigRepository = get(),
                clock = get(),
            )
        }
        single {
            WidgetUpdater(
                context = androidContext(),
                subscriptionRepository = get(),
                settingsRepository = get(),
                widgetConfigRepository = get(),
                clock = get(),
                scope = get(named(WIDGET_SCOPE)),
            )
        }
    }
