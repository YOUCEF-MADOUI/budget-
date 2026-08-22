package com.budgetplusplus.data.repository

import androidx.room.withTransaction
import com.budgetplusplus.core.model.*
import com.budgetplusplus.database.BudgetPlusDatabase
import com.budgetplusplus.database.dao.*
import com.budgetplusplus.database.entity.*
import com.budgetplusplus.domain.futurepayments.FuturePaymentRules
import com.budgetplusplus.domain.repository.FuturePaymentRepository
import java.time.*
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.*

class LocalFuturePaymentRepository @Inject constructor(private val db:BudgetPlusDatabase,private val dao:FuturePaymentDao,private val transactions:TransactionDao,private val accounts:AccountDao,private val categories:CategoryDao):FuturePaymentRepository{
 override fun observeFuturePayments():Flow<List<FuturePayment>> = dao.observeAll().map{rows->val today=LocalDate.now();rows.map{r->FuturePayment(r.id,r.name,r.description,r.plannedAmountMinor,r.paidAmountMinor,r.currencyCode,r.dueDate,r.categoryId,r.categoryNameKey,r.categoryCustomName,r.subcategoryId,r.subcategoryName,r.plannedAccountId,r.plannedAccountName,DuePriority.valueOf(r.priority),r.reminderEnabled,r.reminderDaysBefore,r.notes,r.cancelled,FuturePaymentRules.status(LocalDate.parse(r.dueDate),r.plannedAmountMinor,r.paidAmountMinor,r.cancelled,today))}}
 override fun observePayments(dueId:String):Flow<List<DuePayment>> = dao.observePayments(dueId).map{rows->rows.map{DuePayment(it.id,it.dueId,it.transactionId,it.amountMinor,it.paidDate,it.accountName,it.cancelled)}}
 override suspend fun save(input:FuturePaymentInput)=db.withTransaction{require(FuturePaymentRules.valid(input));val inputId=input.id;if(inputId!=null)require(input.plannedAmountMinor>=dao.paidAmount(inputId));require(categories.kind(input.categoryId)==CategoryKind.EXPENSE);val sub=input.subcategoryId;require(sub==null||categories.subcategoryParent(sub)==input.categoryId);val planned=input.plannedAccountId;if(planned!=null)require(accounts.exists(planned));val old=input.id?.let{dao.get(it)};val now=System.currentTimeMillis();dao.upsert(FuturePaymentEntity(input.id?:UUID.randomUUID().toString(),BudgetPlusDatabase.DEFAULT_WORKSPACE_ID,input.name.trim(),input.description.trim(),input.plannedAmountMinor,input.currencyCode,input.dueDate,input.categoryId,sub,planned,input.priority,input.reminderEnabled,input.reminderDaysBefore,input.notes.trim(),old?.cancelled?:false,old?.createdAt?:now,now))}
 override suspend fun setCancelled(id:String,cancelled:Boolean){dao.setCancelled(id,cancelled,System.currentTimeMillis())}
 override suspend fun pay(dueId:String,requestId:String,accountId:String,amountMinor:Long,paidDate:String):String=db.withTransaction{dao.getByRequest(requestId)?.let{return@withTransaction it.transactionId};val due=requireNotNull(dao.get(dueId));require(!due.cancelled&&accounts.exists(accountId));val paid=dao.paidAmount(dueId);val remaining=Math.subtractExact(due.plannedAmountMinor,paid);require(FuturePaymentRules.validPayment(amountMinor,remaining,paidDate));val now=System.currentTimeMillis();val date=LocalDate.parse(paidDate);val zone=ZoneId.systemDefault();val occurred=date.atTime(12,0).atZone(zone).toInstant().toEpochMilli();val transactionId="due:$requestId";transactions.insert(FinanceTransactionEntity(transactionId,due.workspaceId,TransactionType.EXPENSE,amountMinor,due.currencyCode,accountId,null,due.categoryId,due.subcategoryId,occurred,date.toString(),zone.id,due.name,notes=due.notes,createdAt=now,updatedAt=now));dao.insertPayment(DuePaymentEntity(requestId,dueId,transactionId,amountMinor,date.toString(),now));transactionId}
 override suspend fun cancelPayment(paymentId:String)=db.withTransaction{val payment=requireNotNull(dao.getPayment(paymentId));if(payment.cancelledAt==null&&dao.cancelPayment(paymentId,System.currentTimeMillis())==1)transactions.softDelete(payment.transactionId,System.currentTimeMillis())}
 override suspend fun claimReminders(today:String):List<DueReminder>=db.withTransaction{dao.reminderCandidates(today).mapNotNull{row->val remaining=Math.subtractExact(row.plannedAmountMinor,row.paidAmountMinor);if(remaining<=0)null else if(dao.insertReminderEvent(DueReminderEventEntity(row.dueId,today,System.currentTimeMillis()))==-1L)null else DueReminder(row.dueId,row.name,row.dueDate,remaining,row.currencyCode)}}
}
