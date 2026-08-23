package com.budgetplusplus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budgetplusplus.database.entity.RecurringOccurrenceEntity
import com.budgetplusplus.database.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

data class RecurringDetails(val id:String,val name:String,val type:String,val amountMinor:Long,val currencyCode:String,val accountId:String,val accountName:String,val destinationAccountId:String?,val destinationAccountName:String?,val categoryId:String?,val categoryNameKey:String?,val categoryCustomName:String?,val subcategoryId:String?,val description:String,val frequency:String,val startDate:String,val endDate:String?,val nextDueDate:String,val zoneId:String,val isActive:Boolean)

@Dao interface RecurringDao {
 @Query("""SELECT r.id,r.name,r.type,r.amount_minor AS amountMinor,r.currency_code AS currencyCode,r.account_id AS accountId,a.name AS accountName,r.destination_account_id AS destinationAccountId,d.name AS destinationAccountName,r.category_id AS categoryId,c.name_key AS categoryNameKey,c.custom_name AS categoryCustomName,r.subcategory_id AS subcategoryId,r.description,r.frequency,r.start_date AS startDate,r.end_date AS endDate,r.next_due_date AS nextDueDate,r.zone_id AS zoneId,r.is_active AS isActive FROM recurring_transactions r JOIN accounts a ON a.id=r.account_id LEFT JOIN accounts d ON d.id=r.destination_account_id LEFT JOIN categories c ON c.id=r.category_id WHERE r.deleted_at IS NULL ORDER BY r.is_active DESC,r.next_due_date,r.name""")
 fun observeAll():Flow<List<RecurringDetails>>
 @Query("SELECT * FROM recurring_transactions WHERE deleted_at IS NULL AND is_active=1 ORDER BY next_due_date") suspend fun active():List<RecurringTransactionEntity>
 @Query("SELECT * FROM recurring_transactions WHERE id=:id AND deleted_at IS NULL") suspend fun get(id:String):RecurringTransactionEntity?
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsert(entity:RecurringTransactionEntity)
 @Query("UPDATE recurring_transactions SET is_active=:active,updated_at=:now WHERE id=:id") suspend fun setActive(id:String,active:Boolean,now:Long)
 @Query("UPDATE recurring_transactions SET deleted_at=:now,is_active=0,updated_at=:now WHERE id=:id") suspend fun softDelete(id:String,now:Long)
 @Query("UPDATE recurring_transactions SET next_due_date=:next,updated_at=:now WHERE id=:id") suspend fun updateNext(id:String,next:String,now:Long)
 @Query("SELECT EXISTS(SELECT 1 FROM recurring_occurrences WHERE recurring_transaction_id=:ruleId AND due_date=:date)") suspend fun occurrenceExists(ruleId:String,date:String):Boolean
 @Insert suspend fun insertOccurrence(value:RecurringOccurrenceEntity)
 @Insert suspend fun insertTransaction(value:com.budgetplusplus.database.entity.FinanceTransactionEntity)
 @Query("SELECT * FROM recurring_occurrences ORDER BY processed_at DESC LIMIT 100") fun observeOccurrences():Flow<List<RecurringOccurrenceEntity>>
}
