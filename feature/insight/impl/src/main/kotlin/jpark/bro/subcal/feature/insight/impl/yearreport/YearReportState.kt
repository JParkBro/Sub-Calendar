package jpark.bro.subcal.feature.insight.impl.yearreport

import jpark.bro.subcal.core.domain.insight.TopCategory
import java.time.YearMonth

/**
 * 연말 리포트 (product-spec 4-11). 기간은 1월 1일 ~ 12월 31일(예정 결제 포함), 지출은 내 몫 기준.
 * 시안의 "가장 가성비 좋은 구독" 행은 문서의 카드 4장에 없어 넣지 않는다.
 */
internal data class YearReportState(
    val isLoading: Boolean = true,
    val year: Int = 0,
    /** 칩: 지난해 · 올해 (오래된 순) */
    val years: List<Int> = emptyList(),
    /** 그 해에 결제도 아낀 돈도 없으면 null */
    val content: YearReportContent? = null,
    val page: YearReportPage = YearReportPage.TOTAL,
)

/** 스와이프 카드 4장, 순서대로 */
internal enum class YearReportPage {
    /** 올해 총 지출 · 달별 막대 · 월 평균 */
    TOTAL,

    /** 카테고리 1위 */
    CATEGORY,

    /** 가장 오래 쓴 구독 */
    LONGEST,

    /** 올해 아낀 돈 */
    SAVED,
}

internal data class YearReportContent(
    val totalKrw: Long,
    val monthlyAverageKrw: Long,
    /** 1월 ~ 12월 */
    val monthlyKrw: List<Long>,
    val topCategory: TopCategory?,
    val longest: LongestItem?,
    val savedKrw: Long,
    val savedCount: Int,
)

internal data class LongestItem(
    val name: String,
    val colorKey: String,
    /** 첫 결제 달 */
    val since: YearMonth,
    val yearCount: Int,
)
