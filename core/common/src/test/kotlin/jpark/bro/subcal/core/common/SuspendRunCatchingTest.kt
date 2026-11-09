package jpark.bro.subcal.core.common

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class SuspendRunCatchingTest {
    @Test
    fun `성공하면 값을 담은 Result`() =
        runTest {
            assertEquals(1, suspendRunCatching { 1 }.getOrThrow())
        }

    @Test
    fun `예외는 실패 Result로 바뀐다`() =
        runTest {
            val error = IllegalStateException("실패")

            val result = suspendRunCatching<Int> { throw error }

            assertSame(error, result.exceptionOrNull())
        }

    @Test
    fun `취소 예외는 삼키지 않고 다시 던진다`() {
        assertThrows(CancellationException::class.java) {
            runTest { suspendRunCatching<Int> { throw CancellationException("취소") } }
        }
    }
}
