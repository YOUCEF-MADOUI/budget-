package com.budgetplusplus.core.model

data class BalancePoint(
    val epochMillis: Long,
    val balanceMinor: Long,
    val incomeMinor: Long,
    val expenseMinor: Long,
)

data class DailyCashFlow(
    val epochMillis: Long,
    val incomeMinor: Long,
    val expenseMinor: Long,
)

data class CategoryTotal(
    val categoryId: String,
    val nameKey: String? = null,
    val customName: String? = null,
    val amountMinor: Long,
)

data class DashboardData(
    val totalBalanceMinor: Long = 0,
    val incomeMinor: Long = 0,
    val expenseMinor: Long = 0,
    val accounts: List<Account> = emptyList(),
    val evolution: List<BalancePoint> = emptyList(),
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val recentTransactions: List<FinanceTransaction> = emptyList(),
    val currencyCode: String = "DZD",
)
