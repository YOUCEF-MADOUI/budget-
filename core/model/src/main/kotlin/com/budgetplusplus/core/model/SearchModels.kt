package com.budgetplusplus.core.model

enum class TransactionSort { DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC }

data class TransactionSearchFilter(
 val text:String="",
 val type:TransactionType?=null,
 val accountId:String?=null,
 val categoryId:String?=null,
 val subcategoryId:String?=null,
 val fromDate:String?=null,
 val toDate:String?=null,
 val minimumMinor:Long?=null,
 val maximumMinor:Long?=null,
 val sort:TransactionSort=TransactionSort.DATE_DESC,
)
data class SearchCursor(val value:Long,val id:String)
data class TransactionSearchPage(val items:List<FinanceTransaction>,val nextCursor:SearchCursor?)
