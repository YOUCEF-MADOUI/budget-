package com.budgetplusplus.data.repository

import androidx.room.withTransaction
import com.budgetplusplus.core.model.BudgetInput
import com.budgetplusplus.core.model.BudgetPeriodType
import com.budgetplusplus.core.model.BudgetProgress
import com.budgetplusplus.core.model.BudgetScope
import com.budgetplusplus.database.BudgetPlusDatabase
import com.budgetplusplus.database.dao.BudgetDao
import com.budgetplusplus.database.entity.BudgetAlertThresholdEntity
import com.budgetplusplus.database.entity.BudgetEntity
import com.budgetplusplus.domain.repository.BudgetRepository
import com.budgetplusplus.domain.validation.BudgetValidator
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalBudgetRepository @Inject constructor(private val db: BudgetPlusDatabase, private val dao: BudgetDao) : BudgetRepository {
    override fun observeBudgets(): Flow<List<BudgetProgress>> = dao.observeProgress().map { rows -> rows.map { row ->
        BudgetProgress(row.id, row.name, BudgetScope.valueOf(row.scope), row.categoryId, row.categoryNameKey, row.categoryCustomName, row.amountMinor, row.spentMinor, row.currencyCode, BudgetPeriodType.valueOf(row.periodType), row.startDate, row.endDate, row.warningThresholdPercent, row.isArchived)
    } }

    override suspend fun save(input: BudgetInput) = db.withTransaction {
        require(BudgetValidator.isValid(input))
        val now = System.currentTimeMillis()
        val id = input.id ?: UUID.randomUUID().toString()
        val previous = input.id?.let { dao.get(it) }
        dao.upsert(BudgetEntity(
            id = id, workspaceId = BudgetPlusDatabase.DEFAULT_WORKSPACE_ID, name = input.name.trim(), scope = input.scope,
            categoryId = input.categoryId, amountMinor = input.amountMinor, currencyCode = input.currencyCode,
            periodType = input.periodType, startDate = input.startDate, endDate = input.endDate,
            isArchived = previous?.isArchived ?: false, createdAt = previous?.createdAt ?: now, updatedAt = now,
        ))
        dao.clearThresholds(id)
        dao.upsertThreshold(BudgetAlertThresholdEntity(id, input.warningThresholdPercent))
        dao.upsertThreshold(BudgetAlertThresholdEntity(id, 100))
    }

    override suspend fun setArchived(id: String, archived: Boolean) = dao.setArchived(id, archived, System.currentTimeMillis())
}
