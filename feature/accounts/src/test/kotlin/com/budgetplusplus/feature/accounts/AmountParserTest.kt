package com.budgetplusplus.feature.accounts
import org.junit.Assert.*
import org.junit.Test
class AmountParserTest {
 @Test fun `parses DZD decimal input without floating point`() { assertEquals(125000L, parseAmountMinor("1 250".replace(" ", ""))); assertEquals(1234L, parseAmountMinor("12,34")) }
 @Test fun `rejects excess decimals and overflow`() { assertNull(parseAmountMinor("1,234")); assertNull(parseAmountMinor("999999999999999999999")) }
}
