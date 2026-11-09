package jpark.bro.subcal.core.data.preset

import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Preset
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetSearchTest {
    private val chatGpt = Preset("chatgpt_plus", "ChatGPT Plus", Category.AI, "TEAL", plans = emptyList())

    @Test
    fun `대소문자 · 공백을 무시하고 이름에 검색어가 들어 있으면 맞다`() {
        assertTrue(chatGpt.matches("chat gpt", category = null))
        assertTrue(chatGpt.matches("PLUS", category = null))
        assertTrue(chatGpt.matches("", category = null))
        assertFalse(chatGpt.matches("claude", category = null))
    }

    @Test
    fun `카테고리가 다르면 맞지 않는다`() {
        assertTrue(chatGpt.matches("", Category.AI))
        assertFalse(chatGpt.matches("chat", Category.VIDEO))
    }
}
