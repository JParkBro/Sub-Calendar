package jpark.bro.subcal.core.data.preset

import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Preset

/** 서비스 선택 · 온보딩의 프리셋 검색: [category]가 맞고(null이면 전체), 대소문자 · 공백을 무시한 이름에 [query]가 들어 있으면 */
fun Preset.matches(
    query: String,
    category: Category?,
): Boolean = (category == null || this.category == category) && name.normalized().contains(query.normalized())

private fun String.normalized(): String = filterNot(Char::isWhitespace).lowercase()
