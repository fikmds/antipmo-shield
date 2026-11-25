package com.example

import com.example.data.model.CustomRule
import com.example.service.AntiPmoFilterEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterEngineTest {

    @Test
    fun detects_spaced_adult_keyword() {
        val result = AntiPmoFilterEngine.evaluate("b o k e p")
        assertTrue(result.matched)
        assertEquals("ADULT_18+", result.category)
    }

    @Test
    fun detects_terabox_url() {
        val result = AntiPmoFilterEngine.evaluate("https://terabox.com/s/abc123")
        assertTrue(result.matched)
        assertEquals("TERABOX", result.category)
    }

    @Test
    fun detects_spaced_terabox_keyword() {
        val result = AntiPmoFilterEngine.evaluate("t e r a b o x link")
        assertTrue(result.matched)
        assertEquals("TERABOX", result.category)
    }

    @Test
    fun whitelist_suppresses_match() {
        val result = AntiPmoFilterEngine.evaluate(
            input = "link bokep",
            whitelistedWords = setOf("link bokep")
        )
        assertFalse(result.matched)
        assertEquals("WHITELISTED", result.category)
    }

    @Test
    fun javascript_is_not_blocked() {
        val result = AntiPmoFilterEngine.evaluate("javascript")
        assertFalse(result.matched)
    }

    @Test
    fun custom_regex_rule_matches() {
        val rule = CustomRule(
            pattern = "situsrahasia\\.example",
            isRegex = true,
            category = "CUSTOM"
        )
        val result = AntiPmoFilterEngine.evaluate(
            input = "buka situsrahasia.example sekarang",
            customRules = listOf(rule)
        )
        assertTrue(result.matched)
        assertEquals("CUSTOM", result.category)
    }
}
