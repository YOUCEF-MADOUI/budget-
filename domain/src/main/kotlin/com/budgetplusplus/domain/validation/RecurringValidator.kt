package com.budgetplusplus.domain.validation

import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.core.model.RecurringInput
import com.budgetplusplus.core.model.TransactionType
import java.time.LocalDate
import java.time.ZoneId

object RecurringValidator {
    fun isValid(input: RecurringInput, categoryKind: CategoryKind?): Boolean {
        val start = runCatching { LocalDate.parse(input.startDate) }.getOrNull() ?: return false
        val end = input.endDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() ?: return false }
        if (runCatching { ZoneId.of(input.zoneId) }.isFailure || end != null && end < start || input.name.isBlank()) return false
        return FinanceValidator.validateTransaction(input.type, input.amountMinor, input.accountId, input.destinationAccountId, input.categoryId, categoryKind, input.description)
    }
}
