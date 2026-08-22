package com.budgetplusplus.feature.budgets
import org.junit.Assert.*
import org.junit.Test
class BudgetAmountParserTest {
 @Test fun `parses minor units without floating point`() { assertEquals(250050L, parseMinor("2500,50")) }
 @Test fun `rejects excess decimals and overflow`() { assertNull(parseMinor("1.234")); assertNull(parseMinor("999999999999999999999")) }
}
