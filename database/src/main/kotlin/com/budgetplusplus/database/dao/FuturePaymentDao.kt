package com.budgetplusplus.database.dao

import androidx.room.*
import com.budgetplusplus.database.entity.*
import kotlinx.coroutines.flow.Flow

data class FuturePaymentRow(val id:String,val name:String,val description:String,val plannedAmountMinor:Long,val paidAmountMinor:Long,val currencyCode:String,val dueDate:String,val categoryId:String,val categoryNameKey:String?,val categoryCustomName:String?,val subcategoryId:String?,val subcategoryName:String?,val plannedAccountId:String?,val plannedAccountName:String?,val priority:String,val reminderEnabled:Boolean,val reminderDaysBefore:Int,val notes:String,val cancelled:Boolean)
data class DuePaymentRow(val id:String,val dueId:String,val transactionId:String,val amountMinor:Long,val paidDate:String,val accountName:String,val cancelled:Boolean)
data class DueReminderRow(val dueId:String,val name:String,val dueDate:String,val plannedAmountMinor:Long,val paidAmountMinor:Long,val currencyCode:String)

@Dao interface FuturePaymentDao{
 @Query("""SELECT f.id,f.name,f.description,f.planned_amount_minor AS plannedAmountMinor,COALESCE(SUM(CASE WHEN p.cancelled_at IS NULL AND t.deleted_at IS NULL AND t.type='EXPENSE' THEN t.amount_minor ELSE 0 END),0) AS paidAmountMinor,f.currency_code AS currencyCode,f.due_date AS dueDate,f.category_id AS categoryId,c.name_key AS categoryNameKey,c.custom_name AS categoryCustomName,f.subcategory_id AS subcategoryId,s.custom_name AS subcategoryName,f.planned_account_id AS plannedAccountId,a.name AS plannedAccountName,f.priority,f.reminder_enabled AS reminderEnabled,f.reminder_days_before AS reminderDaysBefore,f.notes,f.cancelled FROM future_payments f JOIN categories c ON c.id=f.category_id LEFT JOIN subcategories s ON s.id=f.subcategory_id LEFT JOIN accounts a ON a.id=f.planned_account_id LEFT JOIN due_payments p ON p.due_id=f.id LEFT JOIN finance_transactions t ON t.id=p.transaction_id WHERE f.deleted_at IS NULL GROUP BY f.id ORDER BY f.cancelled,f.due_date,f.priority DESC""") fun observeAll():Flow<List<FuturePaymentRow>>
 @Query("SELECT * FROM future_payments WHERE id=:id AND deleted_at IS NULL") suspend fun get(id:String):FuturePaymentEntity?
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsert(value:FuturePaymentEntity)
 @Query("UPDATE future_payments SET cancelled=:cancelled,updated_at=:now WHERE id=:id") suspend fun setCancelled(id:String,cancelled:Boolean,now:Long)
 @Query("UPDATE future_payments SET deleted_at=:now,updated_at=:now WHERE id=:id") suspend fun softDelete(id:String,now:Long)
 @Query("""SELECT p.id,p.due_id AS dueId,p.transaction_id AS transactionId,t.amount_minor AS amountMinor,p.paid_date AS paidDate,a.name AS accountName,p.cancelled_at IS NOT NULL OR t.deleted_at IS NOT NULL AS cancelled FROM due_payments p JOIN finance_transactions t ON t.id=p.transaction_id JOIN accounts a ON a.id=t.account_id WHERE p.due_id=:dueId ORDER BY p.created_at DESC""") fun observePayments(dueId:String):Flow<List<DuePaymentRow>>
 @Query("SELECT * FROM due_payments WHERE id=:id") suspend fun getPayment(id:String):DuePaymentEntity?
 @Query("SELECT * FROM due_payments WHERE id=:requestId") suspend fun getByRequest(requestId:String):DuePaymentEntity?
 @Query("SELECT COALESCE(SUM(t.amount_minor),0) FROM due_payments p JOIN finance_transactions t ON t.id=p.transaction_id WHERE p.due_id=:dueId AND p.cancelled_at IS NULL AND t.deleted_at IS NULL AND t.type='EXPENSE'") suspend fun paidAmount(dueId:String):Long
 @Insert suspend fun insertPayment(value:DuePaymentEntity)
 @Query("UPDATE due_payments SET cancelled_at=:now WHERE id=:id AND cancelled_at IS NULL") suspend fun cancelPayment(id:String,now:Long):Int
 @Query("""SELECT f.id AS dueId,f.name,f.due_date AS dueDate,f.planned_amount_minor AS plannedAmountMinor,COALESCE((SELECT SUM(t.amount_minor) FROM due_payments p JOIN finance_transactions t ON t.id=p.transaction_id WHERE p.due_id=f.id AND p.cancelled_at IS NULL AND t.deleted_at IS NULL AND t.type='EXPENSE'),0) AS paidAmountMinor,f.currency_code AS currencyCode FROM future_payments f WHERE f.deleted_at IS NULL AND f.cancelled=0 AND f.reminder_enabled=1 AND date(f.due_date,'-'||f.reminder_days_before||' day')<=:today AND NOT EXISTS(SELECT 1 FROM due_reminder_events e WHERE e.due_id=f.id AND e.reminder_date=:today)""") suspend fun reminderCandidates(today:String):List<DueReminderRow>
 @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun insertReminderEvent(value:DueReminderEventEntity):Long
}
