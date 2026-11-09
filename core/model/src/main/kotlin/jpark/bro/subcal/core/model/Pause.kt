package jpark.bro.subcal.core.model

import java.time.LocalDate

/** 일시정지 기간, 양 끝 포함 (product-spec 2-3). [endDate]가 null이면 일시정지 중 */
data class Pause(
    val id: String,
    val subscriptionId: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    /** epoch millis */
    val createdAt: Long,
)
