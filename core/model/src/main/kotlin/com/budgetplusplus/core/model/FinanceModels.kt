package com.budgetplusplus.core.model

enum class WorkspaceKind { PERSONAL, BUSINESS, FAMILY, OTHER }
enum class AccountType { CASH, BANK, SAVINGS, CARD, OTHER }
enum class CategoryKind { EXPENSE, INCOME }
enum class TransactionType { EXPENSE, INCOME, TRANSFER }

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val currencyCode: String,
    val initialBalanceMinor: Long,
    val currentBalanceMinor: Long,
    val isArchived: Boolean = false,
)

data class Category(
    val id: String,
    val kind: CategoryKind,
    val nameKey: String? = null,
    val customName: String? = null,
    val isSystem: Boolean = false,
    val isArchived: Boolean = false,
)

data class FinanceTransaction(
    val id: String,
    val type: TransactionType,
    val amountMinor: Long,
    val currencyCode: String,
    val accountId: String,
    val accountName: String,
    val destinationAccountId: String? = null,
    val destinationAccountName: String? = null,
    val categoryId: String? = null,
    val categoryNameKey: String? = null,
    val categoryCustomName: String? = null,
    val occurredAt: Long,
    val description: String = "",
)
