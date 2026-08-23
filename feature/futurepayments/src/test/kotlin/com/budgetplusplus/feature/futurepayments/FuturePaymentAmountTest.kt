package com.budgetplusplus.feature.futurepayments
import org.junit.Assert.*
import org.junit.Test
class FuturePaymentAmountTest{@Test fun `planned and paid amounts use exact minor units`(){assertEquals(125050L,parseAmount("1250,50"));assertNull(parseAmount("1.234"));assertNull(parseAmount("999999999999999999999"))}}
