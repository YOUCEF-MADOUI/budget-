package com.budgetplusplus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.budgetplusplus.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

data class AccountWithBalance(
    val id: String, val name: String, val type: String, val currencyCode: String,
    val initialBalanceMinor: Long, val currentBalanceMinor: Long, val isArchived: Boolean,
)

@Dao
interface AccountDao {
    @Query("""SELECT a.id, a.name, a.type, a.currency_code AS currencyCode,
        a.initial_balance_minor AS initialBalanceMinor, a.is_archived AS isArchived,
        a.initial_balance_minor + COALESCE(SUM(CASE
          WHEN t.type = 'INCOME' AND t.account_id = a.id THEN t.amount_minor
          WHEN t.type = 'EXPENSE' AND t.account_id = a.id THEN -t.amount_minor
          WHEN t.type = 'TRANSFER' AND t.account_id = a.id THEN -t.amount_minor
          WHEN t.type = 'TRANSFER' AND t.destination_account_id = a.id THEN t.amount_minor ELSE 0 END), 0) AS currentBalanceMinor
        FROM accounts a LEFT JOIN finance_transactions t ON
          (t.account_id = a.id OR t.destination_account_id = a.id) AND t.deleted_at IS NULL
        WHERE a.deleted_at IS NULL GROUP BY a.id ORDER BY a.is_archived, a.display_order, a.name""")
    fun observeAll(): Flow<List<AccountWithBalance>>

    @Insert suspend fun insert(entity: AccountEntity)
    @Query("UPDATE accounts SET is_archived = :archived, updated_at = :now WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean, now: Long)
    @Query("SELECT EXISTS(SELECT 1 FROM accounts WHERE id = :id AND deleted_at IS NULL)") suspend fun exists(id: String): Boolean
}
