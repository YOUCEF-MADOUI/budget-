package com.budgetplusplus.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class PeriodTotals(val incomeMinor: Long, val expenseMinor: Long)
data class DailyFinancialDelta(val localDate: String, val incomeMinor: Long, val expenseMinor: Long)
data class CategoryFinancialTotal(val categoryId: String, val nameKey: String?, val customName: String?, val amountMinor: Long)

@Dao
interface DashboardDao {
    @Query("""SELECT
        COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount_minor ELSE 0 END), 0) AS incomeMinor,
        COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount_minor ELSE 0 END), 0) AS expenseMinor
        FROM finance_transactions
        WHERE deleted_at IS NULL AND occurred_at >= :fromInclusive AND occurred_at < :toExclusive""")
    fun observePeriodTotals(fromInclusive: Long, toExclusive: Long): Flow<PeriodTotals>

    @Query("""SELECT COALESCE((SELECT SUM(initial_balance_minor) FROM accounts WHERE deleted_at IS NULL), 0) +
        COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount_minor WHEN type = 'EXPENSE' THEN -amount_minor ELSE 0 END), 0)
        FROM finance_transactions WHERE deleted_at IS NULL AND occurred_at < :before""")
    fun observeOpeningBalance(before: Long): Flow<Long>

    @Query("""SELECT local_date AS localDate,
        COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount_minor ELSE 0 END), 0) AS incomeMinor,
        COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount_minor ELSE 0 END), 0) AS expenseMinor
        FROM finance_transactions
        WHERE deleted_at IS NULL AND occurred_at >= :fromInclusive AND occurred_at < :toExclusive
        GROUP BY local_date ORDER BY local_date""")
    fun observeDailyDeltas(fromInclusive: Long, toExclusive: Long): Flow<List<DailyFinancialDelta>>

    @Query("""SELECT c.id AS categoryId, c.name_key AS nameKey, c.custom_name AS customName,
        SUM(t.amount_minor) AS amountMinor
        FROM finance_transactions t JOIN categories c ON c.id = t.category_id
        WHERE t.deleted_at IS NULL AND t.type = 'EXPENSE' AND t.occurred_at >= :fromInclusive AND t.occurred_at < :toExclusive
        GROUP BY c.id ORDER BY amountMinor DESC""")
    fun observeCategoryTotals(fromInclusive: Long, toExclusive: Long): Flow<List<CategoryFinancialTotal>>

    @Query("""SELECT t.id, t.type, t.amount_minor AS amountMinor, t.currency_code AS currencyCode,
        t.account_id AS accountId, a.name AS accountName, t.destination_account_id AS destinationAccountId,
        da.name AS destinationAccountName, t.category_id AS categoryId, c.name_key AS categoryNameKey,
        c.custom_name AS categoryCustomName, t.subcategory_id AS subcategoryId, s.custom_name AS subcategoryName,
        t.occurred_at AS occurredAt, t.local_date AS localDate, t.zone_id AS zoneId, t.description, t.media_id AS mediaId, t.favorite_id AS favoriteId, t.unit_price_minor AS unitPriceMinor, t.quantity
        FROM finance_transactions t JOIN accounts a ON a.id = t.account_id
        LEFT JOIN accounts da ON da.id = t.destination_account_id LEFT JOIN categories c ON c.id = t.category_id
        LEFT JOIN subcategories s ON s.id = t.subcategory_id
        WHERE t.deleted_at IS NULL AND t.occurred_at >= :fromInclusive AND t.occurred_at < :toExclusive
        ORDER BY t.occurred_at DESC LIMIT :limit""")
    fun observeRecent(fromInclusive: Long, toExclusive: Long, limit: Int): Flow<List<TransactionDetails>>
}
