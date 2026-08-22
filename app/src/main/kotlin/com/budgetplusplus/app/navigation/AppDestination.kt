package com.budgetplusplus.app.navigation

internal enum class AppDestination(val route: String) {
    Welcome("welcome"),
    Home("home"),
    DesignSystem("design-system"),
    Accounts("accounts"),
    AccountDetail("accounts/{accountId}"),
    Categories("categories"),
    Transactions("transactions"),
    TransactionNew("transactions/new"),
    TransactionEdit("transactions/edit/{transactionId}"),
    Budgets("budgets"),
    Recurring("recurring"),
    Analytics("analytics"),
    Search("search"),
    Backup("backup"),
    Security("security"),
}
