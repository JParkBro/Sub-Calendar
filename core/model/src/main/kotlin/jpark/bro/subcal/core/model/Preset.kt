package jpark.bro.subcal.core.model

/** 앱에 들어 있는 서비스 목록 (product-spec 2-10) */
data class PresetCatalog(
    val version: Int,
    /** 요금 기준 월 yyyy.MM */
    val pricesAsOf: String,
    val services: List<Preset>,
)

/** [plans]가 비어 있으면 요금을 직접 입력한다 */
data class Preset(
    val id: String,
    val name: String,
    val category: Category,
    val colorKey: String,
    val plans: List<PresetPlan>,
    val cancelUrl: String? = null,
)

data class PresetPlan(
    val name: String,
    val amount: Money,
    val cycle: BillingCycle,
)
