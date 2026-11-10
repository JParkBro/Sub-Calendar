package jpark.bro.subcal.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.data.repository.WidgetConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime

/** 위젯 리시버 · 갱신 작업용 CoroutineScope 이름 */
internal const val WIDGET_SCOPE = "widget"

/**
 * 위젯 갱신 (product-spec 7장, widget/CLAUDE.md).
 * - 구독 · 가격 변경 · 일시정지 · 해지 · 설정(월 시작일 · 환율) · 위젯 설정 · 백업 불러오기 · 모든 데이터 삭제
 *   → Room · DataStore 흐름이 바뀌므로 앱 프로세스가 살아 있는 동안 전체 갱신
 * - 매일 자정: WorkManager 1회 작업을 다음 자정으로 예약하고, 실행되면 다시 예약
 */
class WidgetUpdater(
    private val context: Context,
    private val subscriptionRepository: SubscriptionRepository,
    private val settingsRepository: SettingsRepository,
    private val widgetConfigRepository: WidgetConfigRepository,
    private val clock: Clock,
    private val scope: CoroutineScope,
) {
    /** 앱 시작 시 한 번 (Application) */
    fun start() {
        combine(
            subscriptionRepository.observeRecords(),
            settingsRepository.observeAppSettings(),
            widgetConfigRepository.observeConfigs(),
        ) { _, _, _ -> }
            .onEach {
                updateAll()
                scheduleNextMidnight()
            }.launchIn(scope)
    }

    suspend fun updateAll() {
        SmallPaymentWidget().updateAll(context)
        MediumSummaryWidget().updateAll(context)
    }

    /** 놓인 위젯이 있을 때만 다음 자정 갱신을 예약한다 */
    suspend fun scheduleNextMidnight() {
        val manager = GlanceAppWidgetManager(context)
        val hasWidgets =
            manager.getGlanceIds(SmallPaymentWidget::class.java).isNotEmpty() ||
                manager.getGlanceIds(MediumSummaryWidget::class.java).isNotEmpty()
        // app이 Koin workManagerFactory()로 초기화한 뒤라 꺼낼 수 있다
        val workManager = WorkManager.getInstance(context)
        if (!hasWidgets) {
            workManager.cancelUniqueWork(MIDNIGHT_WORK)
            return
        }
        val request =
            OneTimeWorkRequestBuilder<WidgetMidnightWorker>()
                .setInitialDelay(delayUntilNextMidnight(ZonedDateTime.now(clock)))
                .build()
        workManager.enqueueUniqueWork(MIDNIGHT_WORK, ExistingWorkPolicy.REPLACE, request)
    }

    private companion object {
        const val MIDNIGHT_WORK = "widget_midnight_update"
    }
}

/** 자정 직전에 깨어 어제 날짜로 그리지 않도록 조금 늦게 */
private val MidnightMargin: Duration = Duration.ofMinutes(1)

/** 다음 자정(그 지역 시간 기준) + 1분까지 남은 시간. 서머타임으로 하루가 23 · 25시간이어도 맞게 센다 */
internal fun delayUntilNextMidnight(now: ZonedDateTime): Duration {
    val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
    return Duration.between(now, nextMidnight).plus(MidnightMargin)
}
