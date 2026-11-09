package jpark.bro.subcal.core.common

import kotlin.coroutines.cancellation.CancellationException

/**
 * suspend 블록용 runCatching. 취소(CancellationException)는 삼키지 않고 다시 던진다.
 * Repository의 1회성 작업은 이것으로 Result를 돌려주고, 예외를 ViewModel까지 던지지 않는다.
 */
@Suppress("TooGenericExceptionCaught") // Result로 바꾸는 경계 함수
suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
