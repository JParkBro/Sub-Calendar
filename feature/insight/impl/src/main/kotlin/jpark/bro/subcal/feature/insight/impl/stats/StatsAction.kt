package jpark.bro.subcal.feature.insight.impl.stats

import jpark.bro.subcal.core.model.Category

internal sealed interface StatsAction {
    /** 카테고리 비중 항목: 그 카테고리로 거른 목록 탭 */
    data class CategoryClicked(
        val category: Category,
    ) : StatsAction
}
