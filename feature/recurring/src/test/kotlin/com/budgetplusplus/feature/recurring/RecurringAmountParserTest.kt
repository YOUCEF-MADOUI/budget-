package com.budgetplusplus.feature.recurring
import org.junit.Assert.*
import org.junit.Test
class RecurringAmountParserTest { @Test fun parseMinor(){assertEquals(125050L,parse("1250,50"));assertNull(parse("1.234"))} }
