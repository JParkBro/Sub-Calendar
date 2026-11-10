package jpark.bro.subcal.core.model

import java.time.LocalDate

/** 사용 체크 한 번 (product-spec 4-4, 4-10). 하루에 여러 번 있을 수 있다 */
data class UsageLog(
    val id: String,
    val subscriptionId: String,
    val usedOn: LocalDate,
    /** epoch millis */
    val createdAt: Long,
)
