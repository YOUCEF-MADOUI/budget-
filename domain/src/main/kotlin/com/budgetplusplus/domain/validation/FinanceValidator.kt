package com.budgetplusplus.domain.validation

import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.core.model.TransactionType

object FinanceValidator {
    const val MAX_NAME_LENGTH = 60
    const val MAX_DESCRIPTION_LENGTH = 120

    fun isValidName(name: String): Boolean = name.isNotBlank() && name.trim().length <= MAX_NAME_LENGTH

    fun validateTransaction(
        type: TransactionType,
        amountMinor: Long,
        accountId: String,
        destinationAccountId: String?,
        categoryId: String?,
        categoryKind: CategoryKind?,
        description: String,
    ): Boolean {
        if (amountMinor <= 0 || accountId.isBlank() || description.length > MAX_DESCRIPTION_LENGTH) return false
        return when (type) {
            TransactionType.TRANSFER -> destinationAccountId != null && destinationAccountId != accountId && categoryId == null
            TransactionType.EXPENSE -> destinationAccountId == null && categoryId != null && categoryKind == CategoryKind.EXPENSE
            TransactionType.INCOME -> destinationAccountId == null && categoryId != null && categoryKind == CategoryKind.INCOME
        }
    }
}
