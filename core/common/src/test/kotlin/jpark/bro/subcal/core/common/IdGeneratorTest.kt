package jpark.bro.subcal.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.UUID

class IdGeneratorTest {
    @Test
    fun `UUID 문자열을 만든다`() {
        val id = UuidIdGenerator.newId()

        assertEquals(id, UUID.fromString(id).toString())
    }

    @Test
    fun `호출마다 다른 id`() {
        assertNotEquals(UuidIdGenerator.newId(), UuidIdGenerator.newId())
    }
}
