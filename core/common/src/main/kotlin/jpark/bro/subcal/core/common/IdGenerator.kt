package jpark.bro.subcal.core.common

import java.util.UUID

/** 구독 · 가격 변경 등 행 id 생성. 테스트에서 고정할 수 있게 주입받는다 (CODING_CONVENTIONS 10장) */
fun interface IdGenerator {
    fun newId(): String
}

/** 기본 구현: UUID 문자열 */
object UuidIdGenerator : IdGenerator {
    override fun newId(): String = UUID.randomUUID().toString()
}
