package jpark.bro.subcal.core.model

import java.time.LocalTime

/** 기기에만 저장하는 앱 설정 (data-model-erd 4장) */
data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    /** 월 시작 기준일 (product-spec 2-8) */
    val monthStartDay: Int = 1,
    /** 1달러의 원화 값 (product-spec 2-4) */
    val usdKrwRate: Long = DEFAULT_USD_KRW_RATE,
    val defaultPaymentMethod: String? = null,
) {
    /** 저장할 수 있는 값인지: 월 시작일 1~28, 환율 0보다 큼 */
    val isSelectable: Boolean get() = monthStartDay in MonthStartDays && usdKrwRate > 0

    companion object {
        const val DEFAULT_USD_KRW_RATE = 1_400L
        val MonthStartDays = 1..28
    }
}

/** 알림 설정 (product-spec 6장) */
data class NotificationSettings(
    val paymentEnabled: Boolean = true,
    /** 결제 며칠 전에 알릴지: [DaysBeforeOptions] */
    val defaultDaysBefore: Int = 1,
    val trialEnabled: Boolean = true,
    val partyEnabled: Boolean = true,
    val monthlySummaryEnabled: Boolean = false,
    val time: LocalTime = DefaultTime,
) {
    /** 저장할 수 있는 값인지: 정해진 알림 시점, 오전 8시 ~ 오후 9시 */
    val isSelectable: Boolean
        get() = defaultDaysBefore in DaysBeforeOptions && !time.isBefore(EarliestTime) && !time.isAfter(LatestTime)

    companion object {
        val DaysBeforeOptions = setOf(0, 1, 3, 7)
        val DefaultTime: LocalTime = LocalTime.of(9, 0)
        val EarliestTime: LocalTime = LocalTime.of(8, 0)
        val LatestTime: LocalTime = LocalTime.of(21, 0)
    }
}

/** 목록 정렬 (product-spec 4-2) */
enum class ListSort {
    NEXT_BILLING,
    AMOUNT,
    NAME,
}
