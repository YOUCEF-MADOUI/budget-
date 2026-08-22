package com.budgetplusplus.domain.dashboard

import com.budgetplusplus.core.model.DailyCashFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class BalanceEvolutionCalculatorTest {
    @Test fun `calculates cumulative balance with exact minor units`() {
        val result = BalanceEvolutionCalculator.calculate(1L, 100_00L, listOf(
            DailyCashFlow(3L, 50_00L, 0L),
            DailyCashFlow(2L, 0L, 12_34L),
        ))
        assertEquals(listOf(100_00L, 87_66L, 137_66L), result.map { it.balanceMinor })
    }
}
