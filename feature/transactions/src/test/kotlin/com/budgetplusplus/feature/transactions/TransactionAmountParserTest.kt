package com.budgetplusplus.feature.transactions
import org.junit.Assert.*
import org.junit.Test
class TransactionAmountParserTest {
 @Test fun `stores entered amount in minor units`() { assertEquals(250050L, parseMinor("2500.50")); assertEquals(700L, parseMinor("7")) }
 @Test fun `invalid input is rejected`() { assertNull(parseMinor("1.999")); assertNull(parseMinor("abc")) }
}
