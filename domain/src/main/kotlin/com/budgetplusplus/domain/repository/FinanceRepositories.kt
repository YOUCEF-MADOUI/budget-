package com.budgetplusplus.domain.repository

import com.budgetplusplus.core.model.Account
import com.budgetplusplus.core.model.AccountType
import com.budgetplusplus.core.model.Category
import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.core.model.DashboardData
import com.budgetplusplus.core.model.FinanceTransaction
import com.budgetplusplus.core.model.Subcategory
import com.budgetplusplus.core.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    suspend fun create(name: String, type: AccountType, initialBalanceMinor: Long, currencyCode: String = "DZD")
    suspend fun setArchived(id: String, archived: Boolean)
}

interface CategoryRepository {
    fun observeCategories(kind: CategoryKind? = null): Flow<List<Category>>
    fun observeSubcategories(): Flow<List<Subcategory>>
    suspend fun create(name: String, kind: CategoryKind, iconKey: String = "category", colorKey: String = "primary")
    suspend fun update(id: String, name: String, iconKey: String, colorKey: String)
    suspend fun createSubcategory(categoryId: String, name: String)
    suspend fun updateSubcategory(id: String, name: String)
    suspend fun setArchived(id: String, archived: Boolean, replacementId: String? = null)
    suspend fun setSubcategoryArchived(id: String, archived: Boolean, replacementId: String? = null)
    suspend fun delete(id: String, replacementId: String? = null)
}

interface DashboardRepository {
    fun observeDashboard(fromInclusive: Long, toExclusive: Long): Flow<DashboardData>
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
        subcategoryId: String? = null,
        currencyCode: String = "DZD",
    )
    suspend fun delete(id: String)
}
