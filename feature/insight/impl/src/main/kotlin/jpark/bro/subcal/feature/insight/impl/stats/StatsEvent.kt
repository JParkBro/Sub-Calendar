package jpark.bro.subcal.feature.insight.impl.stats

import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.feature.insight.api.SavingsTab

internal sealed interface StatsEvent {
    data class OpenList(
        val category: Category,
    ) : StatsEvent

    data class OpenSavings(
        val tab: SavingsTab,
    ) : StatsEvent

    data object OpenValueAnalysis : StatsEvent
}
