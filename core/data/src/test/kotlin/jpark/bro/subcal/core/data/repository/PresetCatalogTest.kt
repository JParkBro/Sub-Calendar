package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** 앱에 들어가는 실제 JSON을 검사한다 */
class PresetCatalogTest {
    private val assetJson = File("src/main/assets/presets/services_ko.json").readText()
    private val serviceColors =
        Json
            .parseToJsonElement(File("../../docs/subcal/assets/theme-tokens.json").readText())
            .jsonObject
            .getValue("service")
            .jsonObject.keys

    private suspend fun catalog(dispatcher: TestDispatcher) =
        DefaultPresetRepository({ assetJson }, dispatcher).getCatalog().getOrThrow()

    @Test
    fun `프리셋은 id가 겹치지 않고 색 · 금액이 올바르다`() =
        runTest {
            val catalog = catalog(StandardTestDispatcher(testScheduler))

            assertEquals(
                catalog.services.size,
                catalog.services
                    .map { it.id }
                    .toSet()
                    .size,
            )
            catalog.services.forEach { service ->
                assertTrue(service.id, service.colorKey in serviceColors)
                service.plans.forEach { plan -> assertTrue(service.id, plan.amount.amountMinor > 0) }
            }
        }

    @Test
    fun `요금제의 금액과 주기를 읽는다`() =
        runTest {
            val services = catalog(StandardTestDispatcher(testScheduler)).services.associateBy { it.id }

            assertEquals(
                17_000,
                services
                    .getValue("netflix")
                    .plans[1]
                    .amount.amountMinor,
            )
            assertEquals(
                CycleUnit.YEAR,
                services
                    .getValue("google_one")
                    .plans
                    .single()
                    .cycle.unit,
            )
            assertEquals(
                Currency.USD,
                services
                    .getValue("chatgpt_plus")
                    .plans
                    .single()
                    .amount.currency,
            )
        }

    @Test
    fun `읽기에 실패하면 Result 실패로 돌려준다`() =
        runTest {
            val repository = DefaultPresetRepository({ "깨진 JSON" }, StandardTestDispatcher(testScheduler))

            assertTrue(repository.getCatalog().isFailure)
        }
}
