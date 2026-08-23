package com.budgetplusplus.domain.validation

import com.budgetplusplus.core.model.*
import org.junit.Assert.*
import org.junit.Test

class BudgetValidatorTest {
    private val valid = BudgetInput(name="Mensuel",scope=BudgetScope.GLOBAL,amountMinor=100_00,periodType=BudgetPeriodType.MONTHLY,startDate="2026-08-01",endDate="2026-08-31")
    @Test fun `accepts valid global and category budgets`() {
        assertTrue(BudgetValidator.isValid(valid))
        assertTrue(BudgetValidator.isValid(valid.copy(scope=BudgetScope.CATEGORY,categoryId="food")))
    }
    @Test fun `rejects invalid amount dates scope and threshold`() {
        assertFalse(BudgetValidator.isValid(valid.copy(amountMinor=0)))
        assertFalse(BudgetValidator.isValid(valid.copy(startDate="2026-09-01",endDate="2026-08-01")))
        assertFalse(BudgetValidator.isValid(valid.copy(scope=BudgetScope.CATEGORY,categoryId=null)))
        assertFalse(BudgetValidator.isValid(valid.copy(warningThresholdPercent=100)))
    }
    @Test fun `status uses exact long comparisons`() {
        assertEquals(BudgetStatus.NORMAL, progress(74_99).status)
        assertEquals(BudgetStatus.NEAR_LIMIT, progress(75_00).status)
        assertEquals(BudgetStatus.EXCEEDED, progress(100_01).status)
    }
    private fun progress(spent:Long)=BudgetProgress("id","Budget",BudgetScope.GLOBAL,amountMinor=100_00,spentMinor=spent,currencyCode="DZD",periodType=BudgetPeriodType.MONTHLY,startDate="2026-08-01",endDate="2026-08-31",warningThresholdPercent=75,isArchived=false)
}
