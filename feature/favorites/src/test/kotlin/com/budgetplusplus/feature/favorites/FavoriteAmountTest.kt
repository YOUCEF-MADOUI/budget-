package com.budgetplusplus.feature.favorites
import org.junit.Assert.*
import org.junit.Test
class FavoriteAmountTest{@Test fun `unit price parser keeps exact minor units`(){assertEquals(125L,parse("1,25"));assertNull(parse("1.234"));assertNull(parse("999999999999999999999"))}}
