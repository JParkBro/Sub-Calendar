package jpark.bro.subcal.feature.insight.impl.value

import java.time.YearMonth

/** product-spec 9장 샘플 10월 (유튜브 프리미엄은 파티 4인 내 몫 3,725원 기준) */
@Suppress("MagicNumber") // 샘플 데이터
internal fun previewValueAnalysisState(): ValueAnalysisState =
    ValueAnalysisState(
        isLoading = false,
        month = YearMonth.of(2026, 10),
        items =
            listOf(
                ValueItem("tving", "티빙", "ORANGE", 0, null, 10_900, barFraction = 1f),
                ValueItem("netflix", "넷플릭스 프리미엄", "RED", 2, 8_500, 17_000, barFraction = 1f),
                ValueItem("coupang_wow", "쿠팡 와우", "ORANGE", 15, 526, 7_890, barFraction = 526f / 8_500),
                ValueItem(
                    "youtube_premium",
                    "유튜브 프리미엄",
                    "PINK",
                    20,
                    186,
                    3_725,
                    isParty = true,
                    barFraction =
                        186f / 8_500,
                ),
            ),
        considerSuggestions = listOf(ConsiderSuggestion("tving", "티빙", 10_900)),
    )
