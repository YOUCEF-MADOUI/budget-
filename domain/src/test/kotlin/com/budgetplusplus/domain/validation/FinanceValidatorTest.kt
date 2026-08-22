package com.budgetplusplus.domain.validation

import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.core.model.TransactionType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanceValidatorTest {
    @Test fun `expense requires positive amount and expense category`() {
        assertTrue(FinanceValidator.validateTransaction(TransactionType.EXPENSE, 1L, "account", null, "category", CategoryKind.EXPENSE, ""))
        assertFalse(FinanceValidator.validateTransaction(TransactionType.EXPENSE, 0L, "account", null, "category", CategoryKind.EXPENSE, ""))
        assertFalse(FinanceValidator.validateTransaction(TransactionType.EXPENSE, 1L, "account", null, "category", CategoryKind.INCOME, ""))
    }
    @Test fun `transfer requires a distinct destination and no category`() {
        assertTrue(FinanceValidator.validateTransaction(TransactionType.TRANSFER, 100L, "source", "destination", null, null, ""))
        assertFalse(FinanceValidator.validateTransaction(TransactionType.TRANSFER, 100L, "source", "source", null, null, ""))
    }
    @Test fun `names and descriptions have bounded lengths`() {
        assertTrue(FinanceValidator.isValidName("Compte"))
        assertFalse(FinanceValidator.isValidName(" "))
        assertFalse(FinanceValidator.isValidName("a".repeat(61)))
    }
}
