package jpark.bro.subcal.core.data.preset

import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Preset
import jpark.bro.subcal.core.model.PresetCatalog
import jpark.bro.subcal.core.model.PresetPlan
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** assets/presets/services_ko.json 형식 (product-spec 2-10) */
@Serializable
private data class PresetCatalogJson(
    val version: Int,
    val pricesAsOf: String,
    val services: List<PresetJson>,
)

@Serializable
private data class PresetJson(
    val id: String,
    val name: String,
    val category: Category,
    val colorKey: String,
    val plans: List<PresetPlanJson>,
    val cancelUrl: String? = null,
)

@Serializable
private data class PresetPlanJson(
    val name: String,
    val amountMinor: Long,
    val currency: Currency,
    val cycleUnit: CycleUnit,
    val cycleInterval: Int = 1,
)

internal fun parsePresetCatalog(json: String): PresetCatalog {
    val catalog = Json.decodeFromString<PresetCatalogJson>(json)
    return PresetCatalog(
        version = catalog.version,
        pricesAsOf = catalog.pricesAsOf,
        services =
            catalog.services.map { service ->
                Preset(
                    id = service.id,
                    name = service.name,
                    category = service.category,
                    colorKey = service.colorKey,
                    plans =
                        service.plans.map {
                            PresetPlan(
                                it.name,
                                Money(it.amountMinor, it.currency),
                                BillingCycle(it.cycleUnit, it.cycleInterval),
                            )
                        },
                    cancelUrl = service.cancelUrl,
                )
            },
    )
}
