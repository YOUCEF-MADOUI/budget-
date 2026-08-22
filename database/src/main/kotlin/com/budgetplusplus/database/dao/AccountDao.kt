package com.budgetplusplus.database.dao

import androidx.room.*
import com.budgetplusplus.core.model.AccountType
import com.budgetplusplus.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

data class AccountWithBalance(val id:String,val name:String,val type:String,val currencyCode:String,val initialBalanceMinor:Long,val currentBalanceMinor:Long,val isArchived:Boolean,val iconKey:String,val colorKey:String,val description:String,val displayOrder:Int)
data class AccountTotalsRow(val incomeMinor:Long,val expenseMinor:Long,val operationCount:Int)

@Dao interface AccountDao {
 @Query("""SELECT a.id,a.name,a.type,a.currency_code AS currencyCode,a.initial_balance_minor AS initialBalanceMinor,a.is_archived AS isArchived,a.icon_key AS iconKey,a.color_key AS colorKey,a.description,a.display_order AS displayOrder,a.initial_balance_minor+COALESCE(SUM(CASE WHEN t.type='INCOME' AND t.account_id=a.id THEN t.amount_minor WHEN t.type='EXPENSE' AND t.account_id=a.id THEN -t.amount_minor WHEN t.type='TRANSFER' AND t.account_id=a.id THEN -t.amount_minor WHEN t.type='TRANSFER' AND t.destination_account_id=a.id THEN t.amount_minor ELSE 0 END),0) AS currentBalanceMinor FROM accounts a LEFT JOIN finance_transactions t ON (t.account_id=a.id OR t.destination_account_id=a.id) AND t.deleted_at IS NULL WHERE a.deleted_at IS NULL GROUP BY a.id ORDER BY a.is_archived,a.display_order,a.name""") fun observeAll():Flow<List<AccountWithBalance>>
 @Query("""SELECT COALESCE(SUM(CASE WHEN type='INCOME' AND account_id=:id THEN amount_minor ELSE 0 END),0) AS incomeMinor,COALESCE(SUM(CASE WHEN type='EXPENSE' AND account_id=:id THEN amount_minor ELSE 0 END),0) AS expenseMinor,COUNT(*) AS operationCount FROM finance_transactions WHERE deleted_at IS NULL AND (account_id=:id OR destination_account_id=:id)""") fun observeTotals(id:String):Flow<AccountTotalsRow>
 @Insert suspend fun insert(entity:AccountEntity)
 @Query("SELECT * FROM accounts WHERE id=:id AND deleted_at IS NULL") suspend fun get(id:String):AccountEntity?
 @Query("UPDATE accounts SET name=:name,type=:type,icon_key=:iconKey,color_key=:colorKey,description=:description,display_order=:displayOrder,updated_at=:now WHERE id=:id") suspend fun update(id:String,name:String,type:AccountType,iconKey:String,colorKey:String,description:String,displayOrder:Int,now:Long)
 @Query("UPDATE accounts SET initial_balance_minor=:balance,updated_at=:now WHERE id=:id") suspend fun updateInitialBalance(id:String,balance:Long,now:Long)
 @Query("UPDATE accounts SET is_archived=:archived,updated_at=:now WHERE id=:id") suspend fun setArchived(id:String,archived:Boolean,now:Long)
 @Query("UPDATE accounts SET deleted_at=:now,is_archived=1,updated_at=:now WHERE id=:id") suspend fun softDelete(id:String,now:Long)
 @Query("SELECT COUNT(*) FROM finance_transactions WHERE deleted_at IS NULL AND (account_id=:id OR destination_account_id=:id)") suspend fun operationCount(id:String):Int
 @Query("SELECT COUNT(*) FROM recurring_transactions WHERE deleted_at IS NULL AND (account_id=:id OR destination_account_id=:id)") suspend fun activeRecurrenceCount(id:String):Int
 @Query("SELECT EXISTS(SELECT 1 FROM accounts WHERE id=:id AND deleted_at IS NULL)") suspend fun exists(id:String):Boolean
}
