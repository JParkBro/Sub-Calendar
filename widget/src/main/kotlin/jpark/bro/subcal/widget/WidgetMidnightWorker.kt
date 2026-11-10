package jpark.bro.subcal.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** 자정에 위젯 D-day · 이번 달을 다시 그리고 다음 자정을 예약한다. Worker는 Android 진입점이라 Koin에서 직접 꺼낸다 */
class WidgetMidnightWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params),
    KoinComponent {
    private val widgetUpdater: WidgetUpdater by inject()

    override suspend fun doWork(): Result {
        widgetUpdater.updateAll()
        widgetUpdater.scheduleNextMidnight()
        return Result.success()
    }
}
