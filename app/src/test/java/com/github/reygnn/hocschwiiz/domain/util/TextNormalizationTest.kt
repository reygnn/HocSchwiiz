package com.github.reygnn.hocschwiiz.domain.util

import com.github.reygnn.hocschwiiz.domain.util.TextNormalization.containsNormalized
import com.github.reygnn.hocschwiiz.domain.util.TextNormalization.normalizeForSearch
import com.github.reygnn.hocschwiiz.domain.util.TextNormalization.removeVietnameseTones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextNormalizationTest {

    // ==================== removeVietnameseTones ====================

    @Test
    fun `removeVietnameseTones - basic greeting`() {
        assertEquals("Chao", "Chào".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - multiple tones in phrase`() {
        assertEquals("Bưa sang", "Bữa sáng".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - goodbye phrase`() {
        assertEquals("Tam biêt", "Tạm biệt".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - all a variants lowercase`() {
        assertEquals("aaaaa", "àáảãạ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - all a-breve variants`() {
        assertEquals("ăăăăă", "ằắẳẵặ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - all a-circumflex variants`() {
        assertEquals("âââââ", "ầấẩẫậ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - all e variants`() {
        assertEquals("eeeee", "èéẻẽẹ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - all e-circumflex variants`() {
        assertEquals("êêêêê", "ềếểễệ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - all i variants`() {
        assertEquals("iiiii", "ìíỉĩị".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - all o variants`() {
        assertEquals("ooooo", "òóỏõọ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - all o-circumflex variants`() {
        assertEquals("ôôôôô", "ồốổỗộ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - all o-horn variants`() {
        assertEquals("ơơơơơ", "ờớởỡợ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - all u variants`() {
        assertEquals("uuuuu", "ùúủũụ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - all u-horn variants`() {
        assertEquals("ưưưưư", "ừứửữự".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - all y variants`() {
        assertEquals("yyyyy", "ỳýỷỹỵ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - uppercase A variants`() {
        assertEquals("AAAAA", "ÀÁẢÃẠ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - uppercase A-breve variants`() {
        assertEquals("ĂĂĂĂĂ", "ẰẮẲẴẶ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - uppercase E variants`() {
        assertEquals("EEEEE", "ÈÉẺẼẸ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - uppercase O-horn variants`() {
        assertEquals("ƠƠƠƠƠ", "ỜỚỞỠỢ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - d-stroke lowercase`() {
        assertEquals("d", "đ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - d-stroke uppercase`() {
        assertEquals("D", "Đ".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - mixed case with d-stroke`() {
        assertEquals("Dông", "Đồng".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - preserves base vowels`() {
        // ă, â, ê, ô, ơ, ư should be preserved (they're different vowels, not tones)
        assertEquals("ă â ê ô ơ ư", "ă â ê ô ơ ư".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - empty string`() {
        assertEquals("", "".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - no Vietnamese characters`() {
        assertEquals("Hello World", "Hello World".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - German text unchanged`() {
        assertEquals("Grüezi", "Grüezi".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - Swiss German unchanged`() {
        assertEquals("Guete Tag", "Guete Tag".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - numbers unchanged`() {
        assertEquals("123", "123".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - special characters unchanged`() {
        assertEquals("@#$%", "@#$%".removeVietnameseTones())
    }

    @Test
    fun `removeVietnameseTones - common Vietnamese words`() {
        assertEquals("Xin chao", "Xin chào".removeVietnameseTones())
        assertEquals("Cam ơn", "Cảm ơn".removeVietnameseTones())
        assertEquals("Tiêng Viêt", "Tiếng Việt".removeVietnameseTones())
    }

    // ==================== normalizeForSearch ====================

    @Test
    fun `normalizeForSearch - lowercase conversion`() {
        assertEquals("hello", "HELLO".normalizeForSearch())
    }

    @Test
    fun `normalizeForSearch - trim whitespace`() {
        assertEquals("hello", "  hello  ".normalizeForSearch())
    }

    @Test
    fun `normalizeForSearch - vietnamese with case and whitespace`() {
        assertEquals("xin chao", "  Xin Chào  ".normalizeForSearch())
    }

    @Test
    fun `normalizeForSearch - german umlaut preserved`() {
        assertEquals("grüezi", "GRÜEZI".normalizeForSearch())
    }

    @Test
    fun `normalizeForSearch - empty string`() {
        assertEquals("", "".normalizeForSearch())
    }

    @Test
    fun `normalizeForSearch - whitespace only`() {
        assertEquals("", "   ".normalizeForSearch())
    }

    @Test
    fun `normalizeForSearch - mixed languages`() {
        assertEquals("chao grüezi", "Chào Grüezi".normalizeForSearch())
    }

    // ==================== containsNormalized ====================

    @Test
    fun `containsNormalized - exact match`() {
        assertTrue("Chào".containsNormalized("chào"))
    }

    @Test
    fun `containsNormalized - tone-insensitive match`() {
        assertTrue("Chào".containsNormalized("chao"))
    }

    @Test
    fun `containsNormalized - case-insensitive match`() {
        assertTrue("CHÀO".containsNormalized("chao"))
    }

    @Test
    fun `containsNormalized - partial match`() {
        assertTrue("Xin chào".containsNormalized("chao"))
    }

    @Test
    fun `containsNormalized - no match`() {
        assertFalse("Chào".containsNormalized("tam"))
    }

    @Test
    fun `containsNormalized - german text`() {
        assertTrue("Grüezi".containsNormalized("grüe"))
    }

    @Test
    fun `containsNormalized - swiss german`() {
        assertTrue("Guete Tag".containsNormalized("guete"))
    }

    @Test
    fun `containsNormalized - empty query matches everything`() {
        assertTrue("Anything".containsNormalized(""))
    }

    @Test
    fun `containsNormalized - empty text`() {
        assertFalse("".containsNormalized("query"))
    }

    // ==================== Real-world search scenarios ====================

    @Test
    fun `search scenario - find Bữa sáng by typing bua`() {
        assertTrue("Bữa sáng".containsNormalized("bua"))
    }

    @Test
    fun `search scenario - find Bữa sáng by typing sang`() {
        assertTrue("Bữa sáng".containsNormalized("sang"))
    }

    @Test
    fun `search scenario - find Tạm biệt by typing tam`() {
        assertTrue("Tạm biệt".containsNormalized("tam"))
    }

    @Test
    fun `search scenario - find Tạm biệt by typing biet`() {
        assertTrue("Tạm biệt".containsNormalized("biet"))
    }

    @Test
    fun `search scenario - swiss word search`() {
        assertTrue("Zmorge".containsNormalized("zmor"))
    }

    @Test
    fun `search scenario - german word search`() {
        assertTrue("Frühstück".containsNormalized("früh"))
    }

    @Test
    fun `search scenario - case insensitive swiss`() {
        assertTrue("Uf Widerluege".containsNormalized("WIDERLUEGE"))
    }
}