package com.budgetplusplus.core.designsystem.util

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyFormatterTest {
    @Test
    fun `DZD uses minor units and DA symbol in French`() {
        val formatted = MoneyFormatter.format(125_000_000L, "DZD", Locale.FRANCE)
            .replace('\u202F', ' ')
            .replace('\u00A0', ' ')

        assertEquals("1 250 000,00 DA", formatted)
    }

    @Test
    fun `positive sign is added only when requested`() {
        val unsigned = MoneyFormatter.format(125_000L, "EUR", Locale.FRANCE)
        val signed = MoneyFormatter.format(125_000L, "EUR", Locale.FRANCE, showPositiveSign = true)

        assertTrue(!unsigned.startsWith("+"))
        assertTrue(signed.startsWith("+"))
    }

    @Test
    fun `negative and zero amounts preserve exact minor units`() {
        val negative = MoneyFormatter.format(-580_000L, "DZD", Locale.FRANCE)
        val zero = MoneyFormatter.format(0L, "USD", Locale.US)

        assertTrue(negative.contains("5"))
        assertTrue(negative.contains("800"))
        assertTrue(negative.contains("-"))
        assertEquals("\$0.00", zero)
    }

    @Test
    fun `large long amount formats without floating point conversion`() {
        val formatted = MoneyFormatter.format(Long.MAX_VALUE, "USD", Locale.US)

        assertTrue(formatted.contains("92,233,720,368,547,758.07"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid currency code is rejected`() {
        MoneyFormatter.format(100L, "DA", Locale.FRANCE)
    }
}
