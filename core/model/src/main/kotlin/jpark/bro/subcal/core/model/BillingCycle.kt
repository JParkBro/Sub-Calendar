package jpark.bro.subcal.core.model

/** 결제 주기: [unit]을 [interval]만큼 건너뛴다 (product-spec 2-2). 예: MONTH, 3 = 3개월마다 */
data class BillingCycle(
    val unit: CycleUnit,
    val interval: Int = MIN_INTERVAL,
) {
    init {
        require(interval in MIN_INTERVAL..MAX_INTERVAL) { "interval must be in $MIN_INTERVAL..$MAX_INTERVAL" }
    }

    companion object {
        const val MIN_INTERVAL = 1
        const val MAX_INTERVAL = 99
    }
}

enum class CycleUnit {
    DAY,
    WEEK,
    MONTH,
    YEAR,
}
