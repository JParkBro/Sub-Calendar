package jpark.bro.subcal.core.notification

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class DelayUntilNextTest {
    private val nine = LocalTime.of(9, 0)

    @Test
    fun `알림 시각 전이면 오늘 그 시각까지 기다린다`() {
        assertEquals(Duration.ofMinutes(90), delayUntilNext(LocalDateTime.of(2026, 10, 30, 7, 30), nine))
    }

    @Test
    fun `알림 시각이 지났거나 바로 그 시각이면 내일 같은 시각까지 기다린다`() {
        assertEquals(Duration.ofHours(23), delayUntilNext(LocalDateTime.of(2026, 10, 30, 10, 0), nine))
        assertEquals(Duration.ofHours(24), delayUntilNext(LocalDateTime.of(2026, 10, 30, 9, 0), nine))
    }

    @Test
    fun `월말에는 다음 달 1일로 넘어간다`() {
        assertEquals(
            Duration.ofHours(12),
            delayUntilNext(LocalDateTime.of(2026, 10, 31, 21, 0), nine),
        )
    }
}
