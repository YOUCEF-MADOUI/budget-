package com.budgetplusplus.data.repository

import com.budgetplusplus.core.model.*
import com.budgetplusplus.data.search.TransactionSearchQueryFactory
import com.budgetplusplus.database.dao.SearchDao
import com.budgetplusplus.domain.repository.SearchRepository
import com.budgetplusplus.domain.validation.SearchFilterValidator
import javax.inject.Inject

class LocalSearchRepository @Inject constructor(private val dao:SearchDao):SearchRepository{
 override suspend fun search(filter:TransactionSearchFilter,cursor:SearchCursor?,pageSize:Int):TransactionSearchPage{
  require(pageSize in 1..200)
  require(SearchFilterValidator.isValid(filter))
  val rows=dao.search(TransactionSearchQueryFactory.build(filter,cursor,pageSize+1));val hasMore=rows.size>pageSize;val page=rows.take(pageSize)
  val items=page.map{r->FinanceTransaction(r.id,TransactionType.valueOf(r.type),r.amountMinor,r.currencyCode,r.accountId,r.accountName,r.destinationAccountId,r.destinationAccountName,r.categoryId,r.categoryNameKey,r.categoryCustomName,r.occurredAt,r.description,r.subcategoryId,r.subcategoryName,r.localDate,r.zoneId)}
  val last=page.lastOrNull();val next=if(hasMore&&last!=null)SearchCursor(if(filter.sort==TransactionSort.DATE_DESC||filter.sort==TransactionSort.DATE_ASC)last.occurredAt else last.amountMinor,last.id)else null
  return TransactionSearchPage(items,next)
 }
}
