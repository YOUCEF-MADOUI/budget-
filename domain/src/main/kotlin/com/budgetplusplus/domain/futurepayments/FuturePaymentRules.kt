package com.budgetplusplus.domain.futurepayments

import com.budgetplusplus.core.model.*
import java.time.LocalDate

object FuturePaymentRules {
 fun status(dueDate:LocalDate,planned:Long,paid:Long,cancelled:Boolean,today:LocalDate=LocalDate.now()):DueStatus=when{cancelled->DueStatus.CANCELLED;paid>=planned->DueStatus.PAID;paid>0->DueStatus.PARTIALLY_PAID;dueDate==today->DueStatus.TODAY;dueDate<today->DueStatus.OVERDUE;else->DueStatus.UPCOMING}
 fun valid(input:FuturePaymentInput):Boolean=input.name.isNotBlank()&&input.name.trim().length<=80&&input.description.length<=200&&input.notes.length<=500&&input.plannedAmountMinor>0&&runCatching{LocalDate.parse(input.dueDate)}.isSuccess&&input.categoryId.isNotBlank()&&input.reminderDaysBefore in 0..365
 fun validPayment(amount:Long,remaining:Long,date:String):Boolean=amount>0&&amount<=remaining&&runCatching{LocalDate.parse(date)}.isSuccess
}
