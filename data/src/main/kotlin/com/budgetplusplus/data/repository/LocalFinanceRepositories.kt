package com.budgetplusplus.data.repository

import androidx.room.withTransaction
import com.budgetplusplus.core.model.Account
import com.budgetplusplus.core.model.AccountType
import com.budgetplusplus.core.model.Category
import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.core.model.FinanceTransaction
import com.budgetplusplus.core.model.Subcategory
import com.budgetplusplus.core.model.TransactionType
import com.budgetplusplus.database.BudgetPlusDatabase
import com.budgetplusplus.database.dao.AccountDao
import com.budgetplusplus.database.dao.CategoryDao
import com.budgetplusplus.database.dao.TransactionDao
import com.budgetplusplus.database.entity.AccountEntity
import com.budgetplusplus.database.entity.CategoryEntity
import com.budgetplusplus.database.entity.FinanceTransactionEntity
import com.budgetplusplus.database.entity.SubcategoryEntity
import com.budgetplusplus.domain.repository.AccountRepository
import com.budgetplusplus.domain.repository.CategoryRepository
import com.budgetplusplus.domain.repository.TransactionRepository
import com.budgetplusplus.domain.validation.FinanceValidator
import com.budgetplusplus.domain.validation.CategoryReassignmentValidator
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

class LocalCategoryRepository @Inject constructor(private val db: BudgetPlusDatabase, private val dao: CategoryDao) : CategoryRepository {
    override fun observeCategories(kind: CategoryKind?): Flow<List<Category>> = dao.observeDetails().map { rows -> rows.filter { kind == null || it.kind == kind.name }.map { Category(it.id, CategoryKind.valueOf(it.kind), it.nameKey, it.customName, it.isSystem, it.isArchived, it.iconKey, it.colorKey, it.usageCount) } }
    override fun observeSubcategories(): Flow<List<Subcategory>> = dao.observeSubcategories().map { rows -> rows.map { Subcategory(it.id, it.categoryId, it.customName ?: it.nameKey.orEmpty(), it.isSystem, it.isArchived, it.usageCount) } }
    override suspend fun create(name: String, kind: CategoryKind, iconKey: String, colorKey: String) {
        require(FinanceValidator.isValidName(name)); val now = System.currentTimeMillis()
        dao.insert(CategoryEntity(UUID.randomUUID().toString(), BudgetPlusDatabase.DEFAULT_WORKSPACE_ID, kind, customName = name.trim(), iconKey = iconKey, colorKey = colorKey, createdAt = now, updatedAt = now))
    }
    override suspend fun update(id: String, name: String, iconKey: String, colorKey: String) { require(FinanceValidator.isValidName(name)); require(dao.isSystem(id) == false); dao.update(id, name.trim(), iconKey, colorKey, System.currentTimeMillis()) }
    override suspend fun createSubcategory(categoryId: String, name: String) { require(FinanceValidator.isValidName(name)); require(dao.kind(categoryId) != null); val now = System.currentTimeMillis(); dao.insertSubcategory(SubcategoryEntity(UUID.randomUUID().toString(), categoryId, customName = name.trim(), createdAt = now, updatedAt = now)) }
    override suspend fun updateSubcategory(id: String, name: String) { require(FinanceValidator.isValidName(name)); require(dao.isSubcategorySystem(id) == false); dao.updateSubcategory(id, name.trim(), System.currentTimeMillis()) }
    override suspend fun setArchived(id: String, archived: Boolean, replacementId: String?) = db.withTransaction {
        require(dao.isSystem(id) == false); val usage = dao.usageCount(id); val now = System.currentTimeMillis()
        if (archived && usage > 0) { require(CategoryReassignmentValidator.categoryReplacementIsValid(id, dao.kind(id), usage, replacementId, replacementId?.let { dao.kind(it) })); dao.reassignCategory(id, requireNotNull(replacementId), now) }
        dao.setArchived(id, archived, now)
    }
    override suspend fun setSubcategoryArchived(id: String, archived: Boolean, replacementId: String?) = db.withTransaction {
        require(dao.isSubcategorySystem(id) == false); val usage = dao.subcategoryUsageCount(id); val now = System.currentTimeMillis()
        if (archived && usage > 0) { require(CategoryReassignmentValidator.subcategoryReplacementIsValid(id, dao.subcategoryParent(id), usage, replacementId, replacementId?.let { dao.subcategoryParent(it) })); dao.reassignSubcategory(id, replacementId, now) }
        dao.setSubcategoryArchived(id, archived, now)
    }
    override suspend fun delete(id: String, replacementId: String?) = db.withTransaction {
        require(dao.isSystem(id) == false); val usage = dao.usageCount(id); val now = System.currentTimeMillis()
        if (usage > 0) { require(CategoryReassignmentValidator.categoryReplacementIsValid(id, dao.kind(id), usage, replacementId, replacementId?.let { dao.kind(it) })); dao.reassignCategory(id, requireNotNull(replacementId), now) }
        dao.softDelete(id, now)
    }
}

class LocalTransactionRepository @Inject constructor(
    private val db: BudgetPlusDatabase,
    private val transactions: TransactionDao,
    private val accounts: AccountDao,
    private val categories: CategoryDao,
) : TransactionRepository {
    override fun observeTransactions(): Flow<List<FinanceTransaction>> = transactions.observeAll().map { rows -> rows.map {
        FinanceTransaction(it.id, TransactionType.valueOf(it.type), it.amountMinor, it.currencyCode, it.accountId, it.accountName, it.destinationAccountId, it.destinationAccountName, it.categoryId, it.categoryNameKey, it.categoryCustomName, it.occurredAt, it.description, it.subcategoryId, it.subcategoryName)
    } }

    override suspend fun create(type: TransactionType, amountMinor: Long, accountId: String, destinationAccountId: String?, categoryId: String?, description: String, subcategoryId: String?, currencyCode: String) = db.withTransaction {
        val categoryKind = categoryId?.let { categories.kind(it) }
        require(FinanceValidator.validateTransaction(type, amountMinor, accountId, destinationAccountId, categoryId, categoryKind, description))
        require(subcategoryId == null || categories.subcategoryParent(subcategoryId) == categoryId)
        require(accounts.exists(accountId))
        if (destinationAccountId != null) require(accounts.exists(destinationAccountId))
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        transactions.insert(FinanceTransactionEntity(UUID.randomUUID().toString(), BudgetPlusDatabase.DEFAULT_WORKSPACE_ID, type, amountMinor, currencyCode, accountId, destinationAccountId, categoryId, subcategoryId, now, Instant.ofEpochMilli(now).atZone(zone).toLocalDate().toString(), zone.id, description.trim(), createdAt = now, updatedAt = now))
    }
    override suspend fun delete(id: String) = transactions.softDelete(id, System.currentTimeMillis())
}
