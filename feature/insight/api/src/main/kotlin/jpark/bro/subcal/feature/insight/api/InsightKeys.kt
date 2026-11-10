package jpark.bro.subcal.feature.insight.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 통계 탭 (product-spec 4-6) */
@Serializable
data object StatsKey : NavKey

/** 아낀 돈 · 해지 고민 (product-spec 4-8). [tab]으로 처음 보여줄 탭을 고른다 */
@Serializable
data class SavingsKey(
    val tab: SavingsTab = SavingsTab.CONSIDER,
) : NavKey

@Serializable
enum class SavingsTab {
    /** 해지 고민함 */
    CONSIDER,

    /** 아낀 돈 */
    SAVED,
}
