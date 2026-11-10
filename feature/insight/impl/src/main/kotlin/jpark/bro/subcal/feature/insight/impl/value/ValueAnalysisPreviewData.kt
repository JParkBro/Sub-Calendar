package jpark.bro.subcal.feature.insight.impl.value

import java.time.YearMonth

/** product-spec 9장 샘플 10월 (파티 정산 전이라 유튜브 프리미엄은 전체 금액 기준) */
@Suppress("MagicNumber") // 샘플 데이터
internal fun previewValueAnalysisState(): ValueAnalysisState =
    ValueAnalysisState(
        isLoading = false,
        month = YearMonth.of(2026, 10),
        items =
            listOf(
                ValueItem("tving", "티빙", "ORANGE", 0, null, 10_900, 1f),
                ValueItem("netflix", "넷플릭스 프리미엄", "RED", 2, 8_500, 17_000, 1f),
                ValueItem("youtube_premium", "유튜브 프리미엄", "PINK", 20, 745, 14_900, 745f / 8_500),
                ValueItem("coupang_wow", "쿠팡 와우", "ORANGE", 15, 526, 7_890, 526f / 8_500),
            ),
        considerSuggestions = listOf(ConsiderSuggestion("tving", "티빙", 10_900)),
    )
