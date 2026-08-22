package com.budgetplusplus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.budgetplusplus.database.entity.FinanceTransactionEntity
import kotlinx.coroutines.flow.Flow

data class TransactionDetails(
    val id: String, val type: String, val amountMinor: Long, val currencyCode: String,
    val accountId: String, val accountName: String, val destinationAccountId: String?,
    val destinationAccountName: String?, val categoryId: String?, val categoryNameKey: String?,
    val categoryCustomName: String?, val subcategoryId: String?, val subcategoryName: String?,
    val occurredAt: Long, val localDate: String, val zoneId: String, val description: String,
)

@Dao
interface TransactionDao {
    @Query("""SELECT t.id, t.type, t.amount_minor AS amountMinor, t.currency_code AS currencyCode,
      t.account_id AS accountId, a.name AS accountName, t.destination_account_id AS destinationAccountId,
      da.name AS destinationAccountName, t.category_id AS categoryId, c.name_key AS categoryNameKey,
      c.custom_name AS categoryCustomName, t.subcategory_id AS subcategoryId, s.custom_name AS subcategoryName,
      t.occurred_at AS occurredAt, t.local_date AS localDate, t.zone_id AS zoneId, t.description
      FROM finance_transactions t JOIN accounts a ON a.id = t.account_id
      LEFT JOIN accounts da ON da.id = t.destination_account_id LEFT JOIN categories c ON c.id = t.category_id
      LEFT JOIN subcategories s ON s.id = t.subcategory_id
      WHERE t.deleted_at IS NULL ORDER BY t.occurred_at DESC LIMIT 500""")
    fun observeAll(): Flow<List<TransactionDetails>>
    @Query("SELECT * FROM finance_transactions WHERE id = :id AND deleted_at IS NULL") suspend fun getEntity(id: String): FinanceTransactionEntity?
    @Query("""SELECT t.id, t.type, t.amount_minor AS amountMinor, t.currency_code AS currencyCode,
      t.account_id AS accountId, a.name AS accountName, t.destination_account_id AS destinationAccountId,
      da.name AS destinationAccountName, t.category_id AS categoryId, c.name_key AS categoryNameKey,
      c.custom_name AS categoryCustomName, t.subcategory_id AS subcategoryId, s.custom_name AS subcategoryName,
      t.occurred_at AS occurredAt, t.local_date AS localDate, t.zone_id AS zoneId, t.description FROM finance_transactions t
      JOIN accounts a ON a.id = t.account_id LEFT JOIN accounts da ON da.id = t.destination_account_id
      LEFT JOIN categories c ON c.id = t.category_id LEFT JOIN subcategories s ON s.id = t.subcategory_id
      WHERE t.id = :id AND t.deleted_at IS NULL""") suspend fun getDetails(id: String): TransactionDetails?
    @Insert suspend fun insert(entity: FinanceTransactionEntity)
    @androidx.room.Update suspend fun update(entity: FinanceTransactionEntity)
    @Query("UPDATE finance_transactions SET deleted_at = :now, updated_at = :now WHERE id = :id") suspend fun softDelete(id: String, now: Long)
}
