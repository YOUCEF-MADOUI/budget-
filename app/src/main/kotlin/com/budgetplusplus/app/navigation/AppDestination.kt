package com.budgetplusplus.app.navigation

internal enum class AppDestination(val route: String) {
    Welcome("welcome"),
    Home("home"),
    Accounts("accounts"),
    AccountDetail("accounts/{accountId}"),
    Categories("categories"),
    Transactions("transactions"),
    TransactionNew("transactions/new"),
    TransactionQuick("transactions/new/{type}/{accountId}"),
    TransactionEdit("transactions/edit/{transactionId}"),
    Budgets("budgets"),
    Recurring("recurring"),
    Analytics("analytics"),
    Search("search"),
    Backup("backup"),
    Security("security"),
    FuturePayments("future-payments"),
    Favorites("favorites"),
}
