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
    val iconKey: String = "account",
    val colorKey: String = "primary",
    val description: String = "",
    val displayOrder: Int = 0,
    val parentAccountId: String? = null,
    val mediaId: String? = null,
)

data class AccountOperationTotals(val incomeMinor: Long = 0, val expenseMinor: Long = 0, val operationCount: Int = 0)
data class AccountHierarchyMetrics(
    val ownBalanceMinor: Long = 0,
    val consolidatedBalanceMinor: Long = 0,
    val ownIncomeMinor: Long = 0,
    val ownExpenseMinor: Long = 0,
    val consolidatedIncomeMinor: Long = 0,
    val consolidatedExpenseMinor: Long = 0,
    val descendantCount: Int = 0,
)
data class AccountTreeNode(val account: Account, val depth: Int, val hasChildren: Boolean)

data class Category(
    val id: String,
    val kind: CategoryKind,
    val nameKey: String? = null,
    val customName: String? = null,
    val isSystem: Boolean = false,
    val isArchived: Boolean = false,
    val iconKey: String = "category",
    val colorKey: String = "primary",
    val usageCount: Int = 0,
    val mediaId: String? = null,
)

data class Subcategory(
    val id: String,
    val categoryId: String,
    val name: String,
    val isSystem: Boolean = false,
    val isArchived: Boolean = false,
    val usageCount: Int = 0,
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
    val subcategoryId: String? = null,
    val subcategoryName: String? = null,
    val localDate: String? = null,
    val zoneId: String = "UTC",
    val mediaId: String? = null,
    val favoriteId: String? = null,
    val unitPriceMinor: Long? = null,
    val quantity: Int = 1,
)
