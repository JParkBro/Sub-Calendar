package jpark.bro.subcal.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BillingCycleTest {
    @Test
    fun `간격 기본값은 1`() {
        assertEquals(1, BillingCycle(CycleUnit.MONTH).interval)
    }

    @Test
    fun `간격 1과 99는 허용`() {
        assertEquals(1, BillingCycle(CycleUnit.DAY, interval = 1).interval)
        assertEquals(99, BillingCycle(CycleUnit.DAY, interval = 99).interval)
    }

    @Test
    fun `간격 0과 100은 거부`() {
        assertThrows(IllegalArgumentException::class.java) { BillingCycle(CycleUnit.DAY, interval = 0) }
        assertThrows(IllegalArgumentException::class.java) { BillingCycle(CycleUnit.DAY, interval = 100) }
    }
}
