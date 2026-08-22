package com.budgetplusplus.data.search

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.budgetplusplus.core.model.*

object TransactionSearchQueryFactory{
 fun build(filter:TransactionSearchFilter,cursor:SearchCursor?,limit:Int):SupportSQLiteQuery{
  val sql=StringBuilder("""SELECT t.id,t.type,t.amount_minor AS amountMinor,t.currency_code AS currencyCode,t.account_id AS accountId,a.name AS accountName,t.destination_account_id AS destinationAccountId,d.name AS destinationAccountName,t.category_id AS categoryId,c.name_key AS categoryNameKey,c.custom_name AS categoryCustomName,t.subcategory_id AS subcategoryId,s.custom_name AS subcategoryName,t.occurred_at AS occurredAt,t.local_date AS localDate,t.zone_id AS zoneId,t.description FROM finance_transactions t JOIN accounts a ON a.id=t.account_id LEFT JOIN accounts d ON d.id=t.destination_account_id LEFT JOIN categories c ON c.id=t.category_id LEFT JOIN subcategories s ON s.id=t.subcategory_id WHERE t.deleted_at IS NULL""")
  val args=mutableListOf<Any>()
  fun condition(value:Any?,clause:String){if(value!=null){sql.append(" AND ").append(clause);args+=value}}
  if(filter.text.isNotBlank()){sql.append(" AND t.id IN (SELECT transaction_id FROM finance_transactions_fts WHERE description MATCH ?)");args+=matchExpression(filter.text)}
  condition(filter.type?.name,"t.type=?")
  filter.accountId?.let{sql.append(" AND (t.account_id=? OR t.destination_account_id=?)");args+=it;args+=it}
  condition(filter.categoryId,"t.category_id=?");condition(filter.subcategoryId,"t.subcategory_id=?");condition(filter.fromDate,"t.local_date>=?");condition(filter.toDate,"t.local_date<=?");condition(filter.minimumMinor,"t.amount_minor>=?");condition(filter.maximumMinor,"t.amount_minor<=?")
  val column=if(filter.sort==TransactionSort.DATE_DESC||filter.sort==TransactionSort.DATE_ASC)"t.occurred_at" else "t.amount_minor";val ascending=filter.sort==TransactionSort.DATE_ASC||filter.sort==TransactionSort.AMOUNT_ASC
  cursor?.let{sql.append(if(ascending)" AND ($column>? OR ($column=? AND t.id>?))" else " AND ($column<? OR ($column=? AND t.id<?))");args+=it.value;args+=it.value;args+=it.id}
  sql.append(" ORDER BY $column ").append(if(ascending)"ASC" else "DESC").append(",t.id ").append(if(ascending)"ASC" else "DESC").append(" LIMIT ?");args+=limit
  return SimpleSQLiteQuery(sql.toString(),args.toTypedArray())
 }
 internal fun matchExpression(text:String):String=text.trim().split(Regex("\\s+")).filter{it.isNotBlank()}.joinToString(" AND "){"\"${it.replace("\"","\"\"")}\"*"}
}
