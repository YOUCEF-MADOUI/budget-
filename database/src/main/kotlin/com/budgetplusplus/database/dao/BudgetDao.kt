package com.budgetplusplus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budgetplusplus.database.entity.BudgetAlertThresholdEntity
import com.budgetplusplus.database.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

data class BudgetProgressRow(
    val id: String, val name: String, val scope: String, val categoryId: String?,
    val categoryNameKey: String?, val categoryCustomName: String?, val amountMinor: Long,
    val spentMinor: Long, val currencyCode: String, val periodType: String,
    val startDate: String, val endDate: String, val warningThresholdPercent: Int, val isArchived: Boolean,
)

@Dao
interface BudgetDao {
    @Query("""SELECT b.id, b.name, b.scope, b.category_id AS categoryId,
        c.name_key AS categoryNameKey, c.custom_name AS categoryCustomName,
        b.amount_minor AS amountMinor, b.currency_code AS currencyCode, b.period_type AS periodType,
        b.start_date AS startDate, b.end_date AS endDate, b.is_archived AS isArchived,
        COALESCE((SELECT MAX(percentage) FROM budget_alert_thresholds a WHERE a.budget_id = b.id AND a.enabled = 1 AND a.percentage < 100), 90) AS warningThresholdPercent,
        COALESCE((SELECT SUM(t.amount_minor) FROM finance_transactions t
            WHERE t.deleted_at IS NULL AND t.type = 'EXPENSE'
            AND t.local_date >= b.start_date AND t.local_date <= b.end_date
            AND (b.scope = 'GLOBAL' OR t.category_id = b.category_id)), 0) AS spentMinor
        FROM budgets b LEFT JOIN categories c ON c.id = b.category_id
        WHERE b.deleted_at IS NULL ORDER BY b.is_archived, b.start_date DESC, b.name""")
    fun observeProgress(): Flow<List<BudgetProgressRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entity: BudgetEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertThreshold(entity: BudgetAlertThresholdEntity)
    @Query("DELETE FROM budget_alert_thresholds WHERE budget_id = :budgetId") suspend fun clearThresholds(budgetId: String)
    @Query("UPDATE budgets SET is_archived = :archived, updated_at = :now WHERE id = :id") suspend fun setArchived(id: String, archived: Boolean, now: Long)
    @Query("SELECT * FROM budgets WHERE id = :id AND deleted_at IS NULL") suspend fun get(id: String): BudgetEntity?
}
