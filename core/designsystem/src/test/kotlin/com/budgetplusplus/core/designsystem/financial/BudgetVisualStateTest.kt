package com.budgetplusplus.core.designsystem.financial

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetVisualStateTest {
    @Test
    fun `normal budget remains below warning threshold`() {
        assertEquals(75, budgetUsedPercentage(3_750_000L, 5_000_000L))
        assertEquals(BudgetVisualState.Normal, budgetVisualState(3_750_000L, 5_000_000L))
    }

    @Test
    fun `budget at warning threshold is near limit`() {
        assertEquals(90, budgetUsedPercentage(4_500_000L, 5_000_000L))
        assertEquals(BudgetVisualState.NearLimit, budgetVisualState(4_500_000L, 5_000_000L))
    }

    @Test
    fun `exactly one hundred percent is near limit and overrun is exceeded`() {
        assertEquals(BudgetVisualState.NearLimit, budgetVisualState(5_000_000L, 5_000_000L))
        assertEquals(BudgetVisualState.Exceeded, budgetVisualState(5_000_001L, 5_000_000L))
    }

    @Test
    fun `zero and invalid plans remain safe`() {
        assertEquals(0, budgetUsedPercentage(100L, 0L))
        assertEquals(0, budgetUsedPercentage(-100L, 1_000L))
        assertEquals(BudgetVisualState.Normal, budgetVisualState(100L, 0L))
    }

    @Test
    fun `large values do not overflow percentage`() {
        assertEquals(100, budgetUsedPercentage(Long.MAX_VALUE, Long.MAX_VALUE))
    }
}
