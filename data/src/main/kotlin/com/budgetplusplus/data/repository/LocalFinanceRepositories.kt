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
import com.budgetplusplus.database.dao.TransactionDetails
import com.budgetplusplus.database.entity.AccountEntity
import com.budgetplusplus.database.entity.CategoryEntity
import com.budgetplusplus.database.entity.FinanceTransactionEntity
import com.budgetplusplus.database.entity.SubcategoryEntity
import com.budgetplusplus.domain.repository.AccountRepository
import com.budgetplusplus.domain.repository.CategoryRepository
import com.budgetplusplus.domain.repository.TransactionRepository
import com.budgetplusplus.domain.validation.FinanceValidator
import com.budgetplusplus.domain.validation.CategoryReassignmentValidator
import com.budgetplusplus.domain.accounts.AccountHierarchy
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalAccountRepository @Inject constructor(private val db: BudgetPlusDatabase, private val dao: AccountDao, private val transactions: TransactionDao) : AccountRepository {
    override fun observeAccounts(): Flow<List<Account>> = dao.observeAll().map { rows -> rows.map { Account(it.id, it.name, AccountType.valueOf(it.type), it.currencyCode, it.initialBalanceMinor, it.currentBalanceMinor, it.isArchived, it.iconKey, it.colorKey, it.description, it.displayOrder, it.parentAccountId, it.mediaId) } }
    override fun observeTotals(id: String): Flow<com.budgetplusplus.core.model.AccountOperationTotals> = dao.observeTotals(id).map { com.budgetplusplus.core.model.AccountOperationTotals(it.incomeMinor, it.expenseMinor, it.operationCount) }
    override fun observeHierarchyMetrics(id: String): Flow<com.budgetplusplus.core.model.AccountHierarchyMetrics> = dao.observeHierarchyMetrics(id).map { com.budgetplusplus.core.model.AccountHierarchyMetrics(it.ownBalanceMinor,it.consolidatedBalanceMinor,it.ownIncomeMinor,it.ownExpenseMinor,it.consolidatedIncomeMinor,it.consolidatedExpenseMinor,it.descendantCount) }
    override suspend fun create(name: String, type: AccountType, initialBalanceMinor: Long, currencyCode: String, parentAccountId: String?) {
        require(FinanceValidator.isValidName(name)); val parent=parentAccountId?.let{requireNotNull(dao.get(it))};require(parent==null||parent.currencyCode==currencyCode);val now = System.currentTimeMillis()
        dao.insert(AccountEntity(UUID.randomUUID().toString(), BudgetPlusDatabase.DEFAULT_WORKSPACE_ID, name.trim(), type, currencyCode, initialBalanceMinor, createdAt = now, updatedAt = now, parentAccountId=parentAccountId))
    }
    override suspend fun update(id: String, name: String, type: AccountType, iconKey: String, colorKey: String, description: String, displayOrder: Int) {
        require(FinanceValidator.isValidName(name) && description.length <= 120 && displayOrder >= 0); requireNotNull(dao.get(id)); dao.update(id, name.trim(), type, iconKey, colorKey, description.trim(), displayOrder, System.currentTimeMillis())
    }
    override suspend fun move(id: String, parentAccountId: String?) = db.withTransaction { val all=dao.getAll();val current=requireNotNull(all.firstOrNull{it.id==id});val parent=parentAccountId?.let{pid->requireNotNull(all.firstOrNull{it.id==pid})};require(parent==null||parent.currencyCode==current.currencyCode);require(AccountHierarchy.canMove(id,parentAccountId,all.associate{it.id to it.parentAccountId}));dao.move(id,parentAccountId,System.currentTimeMillis()) }
    override suspend fun setArchived(id: String, archived: Boolean) = db.withTransaction { val now=System.currentTimeMillis();val all=dao.getAll();val ids=AccountHierarchy.descendantIds(id,all.map{it.toAccountModel()})+id;ids.forEach{dao.setArchived(it,archived,now)};if(!archived){val byId=all.associateBy{it.id};var parent=byId[id]?.parentAccountId;val visited=mutableSetOf<String>();while(parent!=null&&visited.add(parent)){dao.setArchived(parent,false,now);parent=byId[parent]?.parentAccountId}} }
    private fun AccountEntity.toAccountModel() = Account(id,name,type,currencyCode,initialBalanceMinor,initialBalanceMinor,isArchived,iconKey,colorKey,description,displayOrder,parentAccountId,mediaId)
    override suspend fun reassignOperations(sourceAccountId: String, targetAccountId: String, transactionIds: Set<String>) = db.withTransaction {
        require(sourceAccountId != targetAccountId && dao.exists(sourceAccountId) && dao.exists(targetAccountId) && transactionIds.isNotEmpty())
        val values = transactions.getEntities(transactionIds); require(values.size == transactionIds.size)
        values.forEach { value -> transactions.update(reassigned(value, sourceAccountId, targetAccountId)) }
    }
    override suspend fun deleteAndReassign(sourceAccountId: String, targetAccountId: String?) = db.withTransaction {
        require(targetAccountId != sourceAccountId && dao.activeRecurrenceCount(sourceAccountId) == 0)
        val all=dao.getAll();val source = requireNotNull(all.firstOrNull{it.id==sourceAccountId});val related=transactions.getRelated(sourceAccountId);val children=all.filter{it.parentAccountId==sourceAccountId};val now=System.currentTimeMillis()
        val target=targetAccountId?.let{tid->requireNotNull(all.firstOrNull{it.id==tid})}
        if(related.isNotEmpty()||children.isNotEmpty())requireNotNull(target)
        if(target!=null){require(target.currencyCode==source.currencyCode);require(AccountHierarchy.canMove(sourceAccountId,target.id,all.associate{it.id to it.parentAccountId}));related.forEach{transactions.update(reassigned(it,sourceAccountId,target.id))};dao.moveChildren(sourceAccountId,target.id,now);dao.updateInitialBalance(target.id,Math.addExact(target.initialBalanceMinor,source.initialBalanceMinor),now)}
        dao.softDelete(sourceAccountId,now)
    }
    private fun reassigned(value: FinanceTransactionEntity, source: String, target: String): FinanceTransactionEntity {
        require(value.accountId == source || value.destinationAccountId == source)
        val account = if (value.accountId == source) target else value.accountId
        val destination = if (value.destinationAccountId == source) target else value.destinationAccountId
        require(value.type != TransactionType.TRANSFER || destination != null && account != destination)
        return value.copy(accountId = account, destinationAccountId = destination, updatedAt = System.currentTimeMillis())
    }
}

class LocalCategoryRepository @Inject constructor(private val db: BudgetPlusDatabase, private val dao: CategoryDao) : CategoryRepository {
    override fun observeCategories(kind: CategoryKind?): Flow<List<Category>> = dao.observeDetails().map { rows -> rows.filter { kind == null || it.kind == kind.name }.map { Category(it.id, CategoryKind.valueOf(it.kind), it.nameKey, it.customName, it.isSystem, it.isArchived, it.iconKey, it.colorKey, it.usageCount, it.mediaId) } }
    override fun observeSubcategories(): Flow<List<Subcategory>> = dao.observeSubcategories().map { rows -> rows.map { Subcategory(it.id, it.categoryId, it.customName ?: it.nameKey.orEmpty(), it.isSystem, it.isArchived, it.usageCount) } }
    override suspend fun create(name: String, kind: CategoryKind, iconKey: String, colorKey: String): String {
        require(FinanceValidator.isValidName(name)); val now = System.currentTimeMillis(); val id = UUID.randomUUID().toString()
        dao.insert(CategoryEntity(id, BudgetPlusDatabase.DEFAULT_WORKSPACE_ID, kind, customName = name.trim(), iconKey = iconKey, colorKey = colorKey, createdAt = now, updatedAt = now))
        return id
    }
    override suspend fun update(id: String, name: String, iconKey: String, colorKey: String) { require(FinanceValidator.isValidName(name)); require(dao.kind(id) != null); dao.update(id, name.trim(), iconKey, colorKey, System.currentTimeMillis()) }
    override suspend fun createSubcategory(categoryId: String, name: String): String { require(FinanceValidator.isValidName(name)); require(dao.kind(categoryId) != null); val now = System.currentTimeMillis(); val id = UUID.randomUUID().toString(); dao.insertSubcategory(SubcategoryEntity(id, categoryId, customName = name.trim(), createdAt = now, updatedAt = now)); return id }
    override suspend fun updateSubcategory(id: String, name: String) { require(FinanceValidator.isValidName(name)); require(dao.isSubcategorySystem(id) == false); dao.updateSubcategory(id, name.trim(), System.currentTimeMillis()) }
    override suspend fun setArchived(id: String, archived: Boolean, replacementId: String?) = db.withTransaction {
        require(dao.kind(id) != null); val usage = dao.usageCount(id); val now = System.currentTimeMillis()
        if (archived && usage > 0) { require(CategoryReassignmentValidator.categoryReplacementIsValid(id, dao.kind(id), usage, replacementId, replacementId?.let { dao.kind(it) })); dao.reassignCategory(id, requireNotNull(replacementId), now) }
        dao.setArchived(id, archived, now)
    }
    override suspend fun setSubcategoryArchived(id: String, archived: Boolean, replacementId: String?) = db.withTransaction {
        require(dao.isSubcategorySystem(id) == false); val usage = dao.subcategoryUsageCount(id); val now = System.currentTimeMillis()
        if (archived && usage > 0) { require(CategoryReassignmentValidator.subcategoryReplacementIsValid(id, dao.subcategoryParent(id), usage, replacementId, replacementId?.let { dao.subcategoryParent(it) })); dao.reassignSubcategory(id, replacementId, now) }
        dao.setSubcategoryArchived(id, archived, now)
    }
    override suspend fun delete(id: String, replacementId: String?) = db.withTransaction {
        require(dao.kind(id) != null); val usage = dao.usageCount(id); val now = System.currentTimeMillis()
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
    override fun observeTransactions(): Flow<List<FinanceTransaction>> = transactions.observeAll().map { rows -> rows.map(::toModel) }
    override suspend fun get(id: String): FinanceTransaction? = transactions.getDetails(id)?.let(::toModel)

    override suspend fun create(type: TransactionType, amountMinor: Long, accountId: String, destinationAccountId: String?, categoryId: String?, description: String, subcategoryId: String?, currencyCode: String, localDate: String?) = db.withTransaction {
        val categoryKind = categoryId?.let { categories.kind(it) }
        require(FinanceValidator.validateTransaction(type, amountMinor, accountId, destinationAccountId, categoryId, categoryKind, description))
        require(subcategoryId == null || categories.subcategoryParent(subcategoryId) == categoryId)
        require(accounts.exists(accountId))
        if (destinationAccountId != null) require(accounts.exists(destinationAccountId))
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val date = localDate?.let(java.time.LocalDate::parse) ?: Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val occurredAt = if (localDate == null) now else date.atTime(Instant.ofEpochMilli(now).atZone(zone).toLocalTime()).atZone(zone).toInstant().toEpochMilli()
        val id = UUID.randomUUID().toString()
        transactions.insert(FinanceTransactionEntity(id, BudgetPlusDatabase.DEFAULT_WORKSPACE_ID, type, amountMinor, currencyCode, accountId, destinationAccountId, categoryId, subcategoryId, occurredAt, date.toString(), zone.id, description.trim(), createdAt = now, updatedAt = now))
        id
    }
    override suspend fun update(id: String, type: TransactionType, amountMinor: Long, accountId: String, destinationAccountId: String?, categoryId: String?, description: String, subcategoryId: String?, localDate: String) = db.withTransaction {
        val existing = requireNotNull(transactions.getEntity(id))
        val categoryKind = categoryId?.let { categories.kind(it) }
        require(FinanceValidator.validateTransaction(type, amountMinor, accountId, destinationAccountId, categoryId, categoryKind, description))
        require(accounts.exists(accountId))
        val destination = destinationAccountId
        if (destination != null) require(accounts.exists(destination))
        val subcategory = subcategoryId
        require(subcategory == null || categories.subcategoryParent(subcategory) == categoryId)
        val date = java.time.LocalDate.parse(localDate)
        val zone = ZoneId.of(existing.zoneId)
        val localTime = Instant.ofEpochMilli(existing.occurredAt).atZone(zone).toLocalTime()
        val occurredAt = date.atTime(localTime).atZone(zone).toInstant().toEpochMilli()
        transactions.update(existing.copy(type = type, amountMinor = amountMinor, accountId = accountId, destinationAccountId = destination, categoryId = categoryId, subcategoryId = subcategory, occurredAt = occurredAt, localDate = date.toString(), description = description.trim(), favoriteId = null, unitPriceMinor = null, quantity = 1, updatedAt = System.currentTimeMillis()))
    }
    override suspend fun delete(id: String) = transactions.softDelete(id, System.currentTimeMillis())
    private fun toModel(value: TransactionDetails) = FinanceTransaction(value.id, TransactionType.valueOf(value.type), value.amountMinor, value.currencyCode, value.accountId, value.accountName, value.destinationAccountId, value.destinationAccountName, value.categoryId, value.categoryNameKey, value.categoryCustomName, value.occurredAt, value.description, value.subcategoryId, value.subcategoryName, value.localDate, value.zoneId, value.mediaId, value.favoriteId, value.unitPriceMinor, value.quantity)
}
