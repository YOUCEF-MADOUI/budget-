package com.budgetplusplus.domain.repository

import com.budgetplusplus.core.model.Account
import com.budgetplusplus.core.model.AnalyticsData
import com.budgetplusplus.core.model.AccountType
import com.budgetplusplus.core.model.BackupPreview
import com.budgetplusplus.core.model.CsvDataset
import com.budgetplusplus.core.model.AppSecuritySettings
import com.budgetplusplus.core.model.PinVerificationResult
import com.budgetplusplus.core.model.BudgetInput
import com.budgetplusplus.core.model.BudgetProgress
import com.budgetplusplus.core.model.Category
import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.core.model.DashboardData
import com.budgetplusplus.core.model.FinanceTransaction
import com.budgetplusplus.core.model.Subcategory
import com.budgetplusplus.core.model.RecurringInput
import com.budgetplusplus.core.model.RecurringOccurrence
import com.budgetplusplus.core.model.RecurringTransaction
import com.budgetplusplus.core.model.TransactionType
import com.budgetplusplus.core.model.TransactionSearchFilter
import com.budgetplusplus.core.model.TransactionSearchPage
import com.budgetplusplus.core.model.SearchCursor
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

interface SecurityRepository {
    fun observeSettings():Flow<AppSecuritySettings>
    suspend fun setPin(pin:CharArray)
    suspend fun verifyPin(pin:CharArray,nowEpochMillis:Long=System.currentTimeMillis()):PinVerificationResult
    suspend fun setBiometricEnabled(enabled:Boolean)
    suspend fun recordBiometricSuccess()
    suspend fun setLockDelay(seconds:Long)
    suspend fun setProtectScreen(enabled:Boolean)
    suspend fun disablePin()
    suspend fun eraseAllDataAfterForgottenPin()
}

interface BackupRepository {
    suspend fun createBackup(destinationUri:String,password:CharArray)
    suspend fun previewBackup(sourceUri:String,password:CharArray):BackupPreview
    suspend fun restoreBackup(sourceUri:String,password:CharArray)
    suspend fun exportCsv(destinationUri:String,dataset:CsvDataset)
}

interface AnalyticsRepository {
    fun observeAnalytics(fromDate:String,toDate:String,previousFromDate:String,previousToDate:String,monthlyBuckets:Boolean): Flow<AnalyticsData>
}

interface BudgetRepository {
    fun observeBudgets(): Flow<List<BudgetProgress>>
    suspend fun save(input: BudgetInput)
    suspend fun setArchived(id: String, archived: Boolean)
}

interface DashboardRepository {
    fun observeDashboard(fromInclusive: Long, toExclusive: Long): Flow<DashboardData>
}

interface RecurringRepository {
    fun observeRecurring(): Flow<List<RecurringTransaction>>
    fun observeOccurrences(): Flow<List<RecurringOccurrence>>
    suspend fun save(input: RecurringInput)
    suspend fun setActive(id: String, active: Boolean)
    suspend fun delete(id: String)
    suspend fun processDue(): Long?
}

interface SearchRepository {
    suspend fun search(filter:TransactionSearchFilter,cursor:SearchCursor?,pageSize:Int=50):TransactionSearchPage
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
