package com.budgetplusplus.domain.repository

import com.budgetplusplus.core.model.Account
import com.budgetplusplus.core.model.AccountType
import com.budgetplusplus.core.model.Category
import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.core.model.FinanceTransaction
import com.budgetplusplus.core.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    suspend fun create(name: String, type: AccountType, initialBalanceMinor: Long, currencyCode: String = "DZD")
    suspend fun setArchived(id: String, archived: Boolean)
}

interface CategoryRepository {
    fun observeCategories(kind: CategoryKind? = null): Flow<List<Category>>
    suspend fun create(name: String, kind: CategoryKind)
    suspend fun setArchived(id: String, archived: Boolean)
}

interface TransactionRepository {
    fun observeTransactions(): Flow<List<FinanceTransaction>>
    suspend fun create(
        type: TransactionType,
        amountMinor: Long,
        accountId: String,
        destinationAccountId: String?,
        categoryId: String?,
        description: String,
        currencyCode: String = "DZD",
    )
    suspend fun delete(id: String)
}
