package jpark.bro.subcal.core.model

/** 불러오기 전에 보여줄 백업 파일 요약 (product-spec 6장) */
data class BackupSummary(
    val subscriptionCount: Int,
    /** 백업을 만든 시각, epoch millis */
    val exportedAt: Long,
)
