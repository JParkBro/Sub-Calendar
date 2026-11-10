package jpark.bro.subcal.feature.insight.impl.stats

import jpark.bro.subcal.core.domain.insight.CategoryShare
import jpark.bro.subcal.core.domain.insight.PeriodTotal
import java.time.LocalDate
import java.time.YearMonth

/**
 * 통계 (product-spec 4-6). 비중 · 12개월 · 연간 환산은 내 부담액(파티 구독은 내 몫) 기준이다.
 * 광고 배너는 출시 단계에서 맨 아래에 붙인다.
 */
internal data class StatsState(
    val isLoading: Boolean = true,
    /** 이번 달 기간 이름이 되는 달 */
    val month: YearMonth = YearMonth.from(LocalDate.ofEpochDay(0)),
    /** 월 시작 기준일이 1이 아니면 기간을 작게 표시 ("10.25 ~ 11.24") */
    val periodStart: LocalDate? = null,
    val periodEndInclusive: LocalDate? = null,
    /** 이번 달 월 환산 기준 카테고리 비중, 큰 순 */
    val categories: List<CategoryShare> = emptyList(),
    /** 카테고리 비중의 합 = 월 평균 */
    val monthlyAverageKrw: Long = 0,
    /** 도넛 가운데 "구독 n개": 비중에 들어간 구독 수 */
    val activeCount: Int = 0,
    /** 최근 12개 기간, 오래된 순. 마지막이 이번 달 */
    val periods: List<PeriodTotal> = emptyList(),
    val thisPeriodKrw: Long = 0,
    /** 지난달 대비 증감 */
    val changeKrw: Long = 0,
    /** "지금처럼 유지하면 1년에" */
    val annualKrw: Long = 0,
    val considerCancelCount: Int = 0,
    /** 해지 고민함 구독을 해지하면 1년에 아끼는 돈 */
    val considerCancelAnnualKrw: Long = 0,
    val savedKrw: Long = 0,
    /** 이용 중 파티 구독이 있으면 "파티 구독은 내 몫만 계산해요" */
    val hasParty: Boolean = false,
    /** 가성비 분석 카드: 사용 체크를 켠 이용 중 구독 수 */
    val valueTrackedCount: Int = 0,
    /** 그중 이번 달 사용 안 함 */
    val valueUnusedCount: Int = 0,
)

/** 최근 몇 개 기간을 보여줄지 (product-spec 4-6 "12개월") */
internal const val RECENT_PERIOD_COUNT = 12
