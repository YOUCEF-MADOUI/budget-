package com.budgetplusplus.domain.recurring

import java.time.LocalDate

object OccurrenceKey {
    fun id(ruleId: String, dueDate: LocalDate): String = "$ruleId:${dueDate}"
    fun transactionId(ruleId: String, dueDate: LocalDate): String = "recurring:${id(ruleId, dueDate)}"
}
