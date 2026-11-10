package jpark.bro.subcal.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import jpark.bro.subcal.core.data.repository.HomeWidgets
import jpark.bro.subcal.core.data.repository.WidgetConfigRepository
import jpark.bro.subcal.core.model.WidgetConfig
import jpark.bro.subcal.core.model.WidgetSize
import jpark.bro.subcal.widget.config.WidgetConfigActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [HomeWidgets] 구현. 놓인 위젯 id는 시스템(AppWidgetManager)에, 설정은 widget_config에 있다 */
internal class DefaultHomeWidgets(
    private val context: Context,
    private val widgetConfigRepository: WidgetConfigRepository,
) : HomeWidgets {
    private val appWidgetManager: AppWidgetManager get() = AppWidgetManager.getInstance(context)

    override fun observePlacedWidgets(): Flow<List<WidgetConfig>> =
        widgetConfigRepository.observeConfigs().map { saved ->
            val byId = saved.associateBy { it.widgetId }
            placedWidgetIds()
                .sortedBy { it.first }
                .map { (id, size) -> byId[id] ?: WidgetConfig(widgetId = id, size = size) }
        }

    override fun isPinSupported(): Boolean = appWidgetManager.isRequestPinAppWidgetSupported

    // 놓이면 리시버의 onUpdate가 기본 설정을 저장하므로 콜백이 필요 없다
    override fun requestPin(size: WidgetSize): Boolean =
        isPinSupported() && appWidgetManager.requestPinAppWidget(context.providerOf(size), null, null)

    override fun openConfigure(widgetId: Int) {
        context.startActivity(
            Intent(context, WidgetConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    /** 홈 화면에 놓인 이 앱 위젯 id와 크기 */
    fun placedWidgetIds(): List<Pair<Int, WidgetSize>> =
        WidgetSize.entries.flatMap { size ->
            appWidgetManager.getAppWidgetIds(context.providerOf(size)).map { it to size }
        }
}

/** 크기별 위젯 리시버 */
internal fun Context.providerOf(size: WidgetSize): ComponentName =
    when (size) {
        WidgetSize.SMALL -> ComponentName(this, SmallPaymentWidgetReceiver::class.java)
        WidgetSize.MEDIUM -> ComponentName(this, MediumSummaryWidgetReceiver::class.java)
    }
