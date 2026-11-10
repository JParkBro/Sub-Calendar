package jpark.bro.subcal.feature.insight.impl.stats

import jpark.bro.subcal.core.model.Category

internal sealed interface StatsAction {
    /** 카테고리 비중 항목: 그 카테고리로 거른 목록 탭 */
    data class CategoryClicked(
        val category: Category,
    ) : StatsAction

    /** 해지 고민함 카드 → 아낀 돈 화면(해지 고민 탭) */
    data object ConsiderCardClicked : StatsAction

    /** 지금까지 아낀 돈 카드 → 아낀 돈 화면(아낀 돈 탭) */
    data object SavedCardClicked : StatsAction

    /** 가성비 분석 카드 → 가성비 분석 */
    data object ValueCardClicked : StatsAction
}
