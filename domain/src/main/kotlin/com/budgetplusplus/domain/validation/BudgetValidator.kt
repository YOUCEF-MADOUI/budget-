package com.budgetplusplus.domain.validation

import com.budgetplusplus.core.model.BudgetInput
import com.budgetplusplus.core.model.BudgetScope
import java.time.LocalDate

object BudgetValidator {
    fun isValid(input: BudgetInput): Boolean {
        val start = runCatching { LocalDate.parse(input.startDate) }.getOrNull() ?: return false
        val end = runCatching { LocalDate.parse(input.endDate) }.getOrNull() ?: return false
        return input.name.isNotBlank() && input.name.trim().length <= 60 &&
            input.amountMinor > 0 && end >= start && input.warningThresholdPercent in 50..99 &&
            ((input.scope == BudgetScope.GLOBAL && input.categoryId == null) || (input.scope == BudgetScope.CATEGORY && input.categoryId != null))
    }
}
