package jpark.bro.subcal.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.data.repository.WidgetConfigRepository
import jpark.bro.subcal.core.model.WidgetBackground
import jpark.bro.subcal.core.model.WidgetSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Clock
import java.time.LocalDate

/** 위젯 한 번 그리기에 필요한 것 */
internal data class WidgetModel(
    val display: WidgetDisplay,
    val colors: WidgetColors,
)

/**
 * 구독 위젯 (Glance). 크기마다 하위 클래스를 두어 위젯 고르기 화면에 두 크기가 따로 보이고 updateAll이 크기별로 동작한다.
 * 위젯은 Android 진입점이라 Koin에서 직접 꺼낸다 (widget/CLAUDE.md 예외).
 * Glance는 세션이 열려 있는 동안 provideGlance를 다시 부르지 않으므로, 내용은 흐름으로 구독해 바뀌면 다시 그린다.
 */
abstract class SubCalWidget(
    private val size: WidgetSize,
) : GlanceAppWidget(),
    KoinComponent {
    private val subscriptionRepository: SubscriptionRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val widgetConfigRepository: WidgetConfigRepository by inject()
    private val clock: Clock by inject()

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val models = observeModel(GlanceAppWidgetManager(context).getAppWidgetId(id))
        val initial = models.first()
        provideContent {
            val model by models.collectAsState(initial)
            WidgetContent(display = model.display, size = size, colors = model.colors)
        }
    }

    private fun observeModel(widgetId: Int): Flow<WidgetModel> =
        combine(
            subscriptionRepository.observeRecords(),
            settingsRepository.observeAppSettings(),
            widgetConfigRepository.observeConfigs(),
        ) { records, settings, configs ->
            val background = configs.firstOrNull { it.widgetId == widgetId }?.background ?: WidgetBackground.THEME
            WidgetModel(
                display = buildWidgetDisplay(records, settings, LocalDate.now(clock)),
                colors = widgetColorScheme(background).toGlance(),
            )
        }.flowOn(Dispatchers.IO)
}

/** 작게 (2×2): 가장 가까운 결제 1건 */
class SmallPaymentWidget : SubCalWidget(WidgetSize.SMALL)

/** 중간 (4×2): 이번 달 구독료 + 다가오는 결제 2건 */
class MediumSummaryWidget : SubCalWidget(WidgetSize.MEDIUM)
