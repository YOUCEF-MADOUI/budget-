package com.budgetplusplus.data.repository

import androidx.room.withTransaction
import com.budgetplusplus.core.model.Account
import com.budgetplusplus.core.model.AccountType
import com.budgetplusplus.core.model.Category
import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.core.model.FinanceTransaction
import com.budgetplusplus.core.model.TransactionType
import com.budgetplusplus.database.BudgetPlusDatabase
import com.budgetplusplus.database.dao.AccountDao
import com.budgetplusplus.database.dao.CategoryDao
import com.budgetplusplus.database.dao.TransactionDao
import com.budgetplusplus.database.entity.AccountEntity
import com.budgetplusplus.database.entity.CategoryEntity
import com.budgetplusplus.database.entity.FinanceTransactionEntity
import com.budgetplusplus.domain.repository.AccountRepository
import com.budgetplusplus.domain.repository.CategoryRepository
import com.budgetplusplus.domain.repository.TransactionRepository
import com.budgetplusplus.domain.validation.FinanceValidator
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalAccountRepository @Inject constructor(private val dao: AccountDao) : AccountRepository {
    override fun observeAccounts(): Flow<List<Account>> = dao.observeAll().map { rows -> rows.map { Account(it.id, it.name, AccountType.valueOf(it.type), it.currencyCode, it.initialBalanceMinor, it.currentBalanceMinor, it.isArchived) } }
    override suspend fun create(name: String, type: AccountType, initialBalanceMinor: Long, currencyCode: String) {
        require(FinanceValidator.isValidName(name))
        val now = System.currentTimeMillis()
        dao.insert(AccountEntity(UUID.randomUUID().toString(), BudgetPlusDatabase.DEFAULT_WORKSPACE_ID, name.trim(), type, currencyCode, initialBalanceMinor, createdAt = now, updatedAt = now))
    }
    override suspend fun setArchived(id: String, archived: Boolean) = dao.setArchived(id, archived, System.currentTimeMillis())
}

class LocalCategoryRepository @Inject constructor(private val dao: CategoryDao) : CategoryRepository {
    override fun observeCategories(kind: CategoryKind?): Flow<List<Category>> = dao.observeAll(kind).map { rows -> rows.map { Category(it.id, it.kind, it.nameKey, it.customName, it.isSystem, it.isArchived) } }
    override suspend fun create(name: String, kind: CategoryKind) {
        require(FinanceValidator.isValidName(name))
        val now = System.currentTimeMillis()
        dao.insert(CategoryEntity(UUID.randomUUID().toString(), BudgetPlusDatabase.DEFAULT_WORKSPACE_ID, kind, customName = name.trim(), createdAt = now, updatedAt = now))
    }
    override suspend fun setArchived(id: String, archived: Boolean) = dao.setArchived(id, archived, System.currentTimeMillis())
}

class LocalTransactionRepository @Inject constructor(
    private val db: BudgetPlusDatabase,
    private val transactions: TransactionDao,
    private val accounts: AccountDao,
    private val categories: CategoryDao,
) : TransactionRepository {
    override fun observeTransactions(): Flow<List<FinanceTransaction>> = transactions.observeAll().map { rows -> rows.map {
        FinanceTransaction(it.id, TransactionType.valueOf(it.type), it.amountMinor, it.currencyCode, it.accountId, it.accountName, it.destinationAccountId, it.destinationAccountName, it.categoryId, it.categoryNameKey, it.categoryCustomName, it.occurredAt, it.description)
    } }

    override suspend fun create(type: TransactionType, amountMinor: Long, accountId: String, destinationAccountId: String?, categoryId: String?, description: String, currencyCode: String) = db.withTransaction {
        val categoryKind = categoryId?.let { categories.kind(it) }
        require(FinanceValidator.validateTransaction(type, amountMinor, accountId, destinationAccountId, categoryId, categoryKind, description))
        require(accounts.exists(accountId))
        if (destinationAccountId != null) require(accounts.exists(destinationAccountId))
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        transactions.insert(FinanceTransactionEntity(UUID.randomUUID().toString(), BudgetPlusDatabase.DEFAULT_WORKSPACE_ID, type, amountMinor, currencyCode, accountId, destinationAccountId, categoryId, now, Instant.ofEpochMilli(now).atZone(zone).toLocalDate().toString(), zone.id, description.trim(), createdAt = now, updatedAt = now))
    }
    override suspend fun delete(id: String) = transactions.softDelete(id, System.currentTimeMillis())
}
