package jpark.bro.subcal.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import jpark.bro.subcal.core.data.repository.WidgetConfigRepository
import jpark.bro.subcal.core.model.WidgetConfig
import jpark.bro.subcal.core.model.WidgetSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

/** 위젯 리시버 공통: 놓이면 기본 설정을 저장하고, 지우면 설정도 지우고, 자정 갱신을 예약한다 */
abstract class SubCalWidgetReceiver :
    GlanceAppWidgetReceiver(),
    KoinComponent {
    protected abstract val size: WidgetSize

    private val widgetConfigRepository: WidgetConfigRepository by inject()
    private val widgetUpdater: WidgetUpdater by inject()
    private val scope: CoroutineScope by inject(named(WIDGET_SCOPE))

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        runAsync { widgetUpdater.scheduleNextMidnight() }
    }

    /** 위젯이 놓이면 기본 설정(테마 배경)을 저장한다. 이미 있으면 그대로 (widget/CLAUDE.md `saveConfigIfAbsent`) */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        runAsync {
            appWidgetIds.forEach { widgetConfigRepository.saveConfigIfAbsent(WidgetConfig(widgetId = it, size = size)) }
        }
    }

    override fun onDeleted(
        context: Context,
        appWidgetIds: IntArray,
    ) {
        super.onDeleted(context, appWidgetIds)
        runAsync {
            widgetConfigRepository.deleteConfigs(appWidgetIds.toList())
            widgetUpdater.scheduleNextMidnight()
        }
    }

    /**
     * GlanceAppWidgetReceiver.onUpdate가 이미 goAsync()를 불렀으면 두 번째 호출은 null을 준다
     * (그때는 Glance가 잡은 대기 결과가 리시버를 살려 둔다)
     */
    private fun runAsync(block: suspend () -> Unit) {
        val pending: PendingResult? = goAsync()
        scope.launch {
            try {
                block()
            } finally {
                pending?.finish()
            }
        }
    }
}

class SmallPaymentWidgetReceiver : SubCalWidgetReceiver() {
    override val size: WidgetSize = WidgetSize.SMALL
    override val glanceAppWidget: GlanceAppWidget = SmallPaymentWidget()
}

class MediumSummaryWidgetReceiver : SubCalWidgetReceiver() {
    override val size: WidgetSize = WidgetSize.MEDIUM
    override val glanceAppWidget: GlanceAppWidget = MediumSummaryWidget()
}

/** 기기 시각 · 시간대가 바뀌면 D-day를 다시 그리고 자정 예약을 다시 잡는다 */
class WidgetTimeChangedReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val widgetUpdater: WidgetUpdater by inject()
    private val scope: CoroutineScope by inject(named(WIDGET_SCOPE))

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action !in TimeActions) return
        val pending = goAsync()
        scope.launch {
            try {
                widgetUpdater.updateAll()
                widgetUpdater.scheduleNextMidnight()
            } finally {
                pending?.finish()
            }
        }
    }

    private companion object {
        val TimeActions = setOf(Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)
    }
}
