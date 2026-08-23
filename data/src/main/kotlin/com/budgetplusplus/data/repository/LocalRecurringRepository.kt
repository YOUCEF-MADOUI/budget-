package com.budgetplusplus.data.repository

import androidx.room.withTransaction
import com.budgetplusplus.core.model.*
import com.budgetplusplus.database.BudgetPlusDatabase
import com.budgetplusplus.database.dao.*
import com.budgetplusplus.database.entity.*
import com.budgetplusplus.domain.recurring.RecurrenceCalculator
import com.budgetplusplus.domain.recurring.OccurrenceKey
import com.budgetplusplus.domain.repository.RecurringRepository
import com.budgetplusplus.domain.validation.RecurringValidator
import java.time.*
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalRecurringRepository @Inject constructor(private val db:BudgetPlusDatabase,private val dao:RecurringDao,private val categories:CategoryDao,private val accounts:AccountDao):RecurringRepository{
 override fun observeRecurring():Flow<List<RecurringTransaction>> = dao.observeAll().map{rows->rows.map{r->RecurringTransaction(r.id,r.name,TransactionType.valueOf(r.type),r.amountMinor,r.currencyCode,r.accountId,r.accountName,r.destinationAccountId,r.destinationAccountName,r.categoryId,r.categoryNameKey,r.categoryCustomName,r.subcategoryId,r.description,RecurrenceFrequency.valueOf(r.frequency),r.startDate,r.endDate,r.nextDueDate,r.zoneId,r.isActive)}}
 override fun observeOccurrences():Flow<List<RecurringOccurrence>> = dao.observeOccurrences().map{list->list.map{RecurringOccurrence(it.id,it.recurringTransactionId,it.dueDate,it.status,it.transactionId,it.errorCode,it.processedAt)}}
 override suspend fun save(input:RecurringInput)=db.withTransaction{
  val kind=input.categoryId?.let{categories.kind(it)};require(RecurringValidator.isValid(input,kind));require(accounts.exists(input.accountId));val destination=input.destinationAccountId;if(destination!=null)require(accounts.exists(destination));val subcategory=input.subcategoryId;require(subcategory==null||categories.subcategoryParent(subcategory)==input.categoryId)
  val now=System.currentTimeMillis();val id=input.id?:UUID.randomUUID().toString();val old=input.id?.let{dao.get(it)};val start=LocalDate.parse(input.startDate)
  dao.upsert(RecurringTransactionEntity(id,BudgetPlusDatabase.DEFAULT_WORKSPACE_ID,input.name.trim(),input.type,input.amountMinor,"DZD",input.accountId,input.destinationAccountId,input.categoryId,input.subcategoryId,input.description.trim(),input.frequency,start.monthValue,start.dayOfMonth,input.startDate,input.endDate,if(old==null)input.startDate else old.nextDueDate,input.zoneId,old?.isActive?:true,old?.createdAt?:now,now))
 }
 override suspend fun setActive(id:String,active:Boolean){dao.setActive(id,active,System.currentTimeMillis())}
 override suspend fun delete(id:String){dao.softDelete(id,System.currentTimeMillis())}
 override suspend fun processDue():Long?{
  dao.active().forEach{rule->processRule(rule)}
  return dao.active().mapNotNull{rule->runCatching{val next=LocalDate.parse(rule.nextDueDate);val end=rule.endDate?.let(LocalDate::parse);if(end!=null&&next.isAfter(end))null else next.atTime(9,0).atZone(ZoneId.of(rule.zoneId)).toInstant().toEpochMilli()}.getOrNull()}.minOrNull()
 }
 private suspend fun processRule(rule:RecurringTransactionEntity){
  val zone=ZoneId.of(rule.zoneId);val today=LocalDate.now(zone);var due=LocalDate.parse(rule.nextDueDate);val end=rule.endDate?.let(LocalDate::parse);var count=0
  while(!due.isAfter(today)&&(end==null||!due.isAfter(end))&&count++<5000){
   val dueText=due.toString();val occurrenceId=OccurrenceKey.id(rule.id,due);val transactionId=OccurrenceKey.transactionId(rule.id,due);val processed=System.currentTimeMillis()
   if(!dao.occurrenceExists(rule.id,dueText)){
    runCatching{db.withTransaction{
     if(!dao.occurrenceExists(rule.id,dueText)){
      val instant=due.atTime(9,0).atZone(zone).toInstant().toEpochMilli()
      dao.insertTransaction(FinanceTransactionEntity(transactionId,rule.workspaceId,rule.type,rule.amountMinor,rule.currencyCode,rule.accountId,rule.destinationAccountId,rule.categoryId,rule.subcategoryId,instant,dueText,zone.id,rule.description,createdAt=processed,updatedAt=processed))
      dao.insertOccurrence(RecurringOccurrenceEntity(occurrenceId,rule.id,dueText,RecurrenceOccurrenceStatus.CREATED,transactionId,null,processed))
     }
    }}.onFailure{runCatching{dao.insertOccurrence(RecurringOccurrenceEntity(occurrenceId,rule.id,dueText,RecurrenceOccurrenceStatus.FAILED,null,"PROCESSING_FAILED",processed))}}
   }
   due=RecurrenceCalculator.nextAfter(due,rule.frequency,rule.anchorMonth,rule.anchorDay)
   dao.updateNext(rule.id,due.toString(),System.currentTimeMillis())
  }
 }
}
