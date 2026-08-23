package com.budgetplusplus.core.model

enum class RecurrenceFrequency { DAILY, WEEKLY, MONTHLY, YEARLY }
enum class RecurrenceOccurrenceStatus { CREATED, FAILED }

data class RecurringInput(
    val id: String? = null,
    val name: String,
    val type: TransactionType,
    val amountMinor: Long,
    val accountId: String,
    val destinationAccountId: String? = null,
    val categoryId: String? = null,
    val subcategoryId: String? = null,
    val description: String = "",
    val frequency: RecurrenceFrequency,
    val startDate: String,
    val endDate: String? = null,
    val zoneId: String,
)

data class RecurringTransaction(
    val id: String,
    val name: String,
    val type: TransactionType,
    val amountMinor: Long,
    val currencyCode: String,
    val accountId: String,
    val accountName: String,
    val destinationAccountId: String?,
    val destinationAccountName: String?,
    val categoryId: String?,
    val categoryNameKey: String?,
    val categoryCustomName: String?,
    val subcategoryId: String?,
    val description: String,
    val frequency: RecurrenceFrequency,
    val startDate: String,
    val endDate: String?,
    val nextDueDate: String,
    val zoneId: String,
    val isActive: Boolean,
)

data class RecurringOccurrence(
    val id: String,
    val recurringTransactionId: String,
    val dueDate: String,
    val status: RecurrenceOccurrenceStatus,
    val transactionId: String?,
    val errorCode: String?,
    val processedAt: Long,
)
