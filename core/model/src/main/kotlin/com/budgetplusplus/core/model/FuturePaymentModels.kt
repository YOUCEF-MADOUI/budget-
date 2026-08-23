package com.budgetplusplus.core.model

enum class DuePriority { LOW, NORMAL, HIGH, URGENT }
enum class DueStatus { UPCOMING, TODAY, OVERDUE, PARTIALLY_PAID, PAID, CANCELLED }

data class FuturePaymentInput(
 val id:String?=null,val name:String,val description:String="",val plannedAmountMinor:Long,val currencyCode:String="DZD",val dueDate:String,val categoryId:String,val subcategoryId:String?=null,val plannedAccountId:String?=null,val priority:DuePriority=DuePriority.NORMAL,val reminderEnabled:Boolean=false,val reminderDaysBefore:Int=1,val notes:String=""
)
data class FuturePayment(
 val id:String,val name:String,val description:String,val plannedAmountMinor:Long,val paidAmountMinor:Long,val currencyCode:String,val dueDate:String,val categoryId:String,val categoryNameKey:String?,val categoryCustomName:String?,val subcategoryId:String?,val subcategoryName:String?,val plannedAccountId:String?,val plannedAccountName:String?,val priority:DuePriority,val reminderEnabled:Boolean,val reminderDaysBefore:Int,val notes:String,val cancelled:Boolean,val status:DueStatus
){val remainingMinor:Long get()=Math.subtractExact(plannedAmountMinor,paidAmountMinor)}
data class DuePayment(val id:String,val dueId:String,val transactionId:String,val amountMinor:Long,val paidDate:String,val accountName:String,val cancelled:Boolean)
data class DueReminder(val dueId:String,val name:String,val dueDate:String,val remainingMinor:Long,val currencyCode:String)
