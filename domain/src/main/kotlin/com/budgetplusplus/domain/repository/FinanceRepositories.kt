package com.budgetplusplus.domain.repository

import com.budgetplusplus.core.model.Account
import com.budgetplusplus.core.model.AnalyticsData
import com.budgetplusplus.core.model.AccountType
import com.budgetplusplus.core.model.BackupPreview
import com.budgetplusplus.core.model.FuturePayment
import com.budgetplusplus.core.model.MediaAsset
import com.budgetplusplus.core.model.CropMode
import com.budgetplusplus.core.model.Favorite
import com.budgetplusplus.core.model.FavoriteInput
import com.budgetplusplus.core.model.FavoriteClickResult
import com.budgetplusplus.core.model.FuturePaymentInput
import com.budgetplusplus.core.model.DuePayment
import com.budgetplusplus.core.model.DueReminder
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
    fun observeTotals(id: String): Flow<com.budgetplusplus.core.model.AccountOperationTotals>
    fun observeHierarchyMetrics(id: String): Flow<com.budgetplusplus.core.model.AccountHierarchyMetrics>
    suspend fun create(name: String, type: AccountType, initialBalanceMinor: Long, currencyCode: String = "DZD", parentAccountId: String? = null)
    suspend fun update(id: String, name: String, type: AccountType, iconKey: String, colorKey: String, description: String, displayOrder: Int)
    suspend fun move(id: String, parentAccountId: String?)
    suspend fun setArchived(id: String, archived: Boolean)
    suspend fun reassignOperations(sourceAccountId: String, targetAccountId: String, transactionIds: Set<String>)
    suspend fun deleteAndReassign(sourceAccountId: String, targetAccountId: String?)
}

interface CategoryRepository {
    fun observeCategories(kind: CategoryKind? = null): Flow<List<Category>>
    fun observeSubcategories(): Flow<List<Subcategory>>
    suspend fun create(name: String, kind: CategoryKind, iconKey: String = "category", colorKey: String = "primary"): String
    suspend fun update(id: String, name: String, iconKey: String, colorKey: String)
    suspend fun createSubcategory(categoryId: String, name: String): String
    suspend fun updateSubcategory(id: String, name: String)
    suspend fun setArchived(id: String, archived: Boolean, replacementId: String? = null)
    suspend fun setSubcategoryArchived(id: String, archived: Boolean, replacementId: String? = null)
    suspend fun delete(id: String, replacementId: String? = null)
}

interface MediaRepository {
    suspend fun importMedia(sourceUri:String,cropMode:CropMode):MediaAsset
    suspend fun attachToAccount(accountId:String,mediaId:String?)
    suspend fun attachToCategory(categoryId:String,mediaId:String?)
    suspend fun attachToTransaction(transactionId:String,mediaId:String?)
    suspend fun mediaFilePath(mediaId:String,thumbnail:Boolean=true):String?
}

interface FavoriteRepository {
    fun observeFavorites():Flow<List<Favorite>>
    suspend fun save(input:FavoriteInput):String
    suspend fun setArchived(id:String,archived:Boolean)
    suspend fun recordImmediateClick(favoriteId:String,nowEpochMillis:Long=System.currentTimeMillis()):FavoriteClickResult
    suspend fun createOperation(favoriteId:String,requestId:String,quantity:Int,unitPriceMinor:Long,date:String):FavoriteClickResult
    suspend fun undo(transactionId:String)
}

interface FuturePaymentRepository {
    fun observeFuturePayments():Flow<List<FuturePayment>>
    fun observePayments(dueId:String):Flow<List<DuePayment>>
    suspend fun save(input:FuturePaymentInput)
    suspend fun setCancelled(id:String,cancelled:Boolean)
    suspend fun pay(dueId:String,requestId:String,accountId:String,amountMinor:Long,paidDate:String):String
    suspend fun cancelPayment(paymentId:String)
    suspend fun claimReminders(today:String):List<DueReminder>
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
    suspend fun get(id: String): FinanceTransaction?
    suspend fun create(
        type: TransactionType,
        amountMinor: Long,
        accountId: String,
        destinationAccountId: String?,
        categoryId: String?,
        description: String,
        subcategoryId: String? = null,
        currencyCode: String = "DZD",
        localDate: String? = null,
    ): String
    suspend fun update(
        id: String,
        type: TransactionType,
        amountMinor: Long,
        accountId: String,
        destinationAccountId: String?,
        categoryId: String?,
        description: String,
        subcategoryId: String?,
        localDate: String,
    )
    suspend fun delete(id: String)
}
