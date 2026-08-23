package com.budgetplusplus.feature.search
import org.junit.Assert.*
import org.junit.Test
class SearchAmountParserTest{@Test fun parseBounds(){assertEquals(1234L,parse("12,34"));assertNull(parse("1.234"));assertNull(parse(""))}}
