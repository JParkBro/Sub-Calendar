package jpark.bro.subcal.feature.insight.impl.stats

import jpark.bro.subcal.core.model.Category

internal sealed interface StatsEvent {
    data class OpenList(
        val category: Category,
    ) : StatsEvent
}
