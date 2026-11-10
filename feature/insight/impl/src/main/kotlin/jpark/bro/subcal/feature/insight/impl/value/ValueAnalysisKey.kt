package jpark.bro.subcal.feature.insight.impl.value

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 통계 → 가성비 분석 (product-spec 4-10, module-map 4-2 내부 화면) */
@Serializable
internal data object ValueAnalysisKey : NavKey
