package com.budgetplusplus.database

import androidx.room.TypeConverter
import com.budgetplusplus.core.model.AccountType
import com.budgetplusplus.core.model.BudgetPeriodType
import com.budgetplusplus.core.model.BudgetScope
import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.core.model.RecurrenceFrequency
import com.budgetplusplus.core.model.DuePriority
import com.budgetplusplus.core.model.RecurrenceOccurrenceStatus
import com.budgetplusplus.core.model.TransactionType

class RoomConverters {
    @TypeConverter fun accountType(value: AccountType): String = value.name
    @TypeConverter fun accountType(value: String): AccountType = AccountType.valueOf(value)
    @TypeConverter fun categoryKind(value: CategoryKind): String = value.name
    @TypeConverter fun categoryKind(value: String): CategoryKind = CategoryKind.valueOf(value)
    @TypeConverter fun transactionType(value: TransactionType): String = value.name
    @TypeConverter fun transactionType(value: String): TransactionType = TransactionType.valueOf(value)
    @TypeConverter fun budgetScope(value: BudgetScope): String = value.name
    @TypeConverter fun budgetScope(value: String): BudgetScope = BudgetScope.valueOf(value)
    @TypeConverter fun budgetPeriodType(value: BudgetPeriodType): String = value.name
    @TypeConverter fun budgetPeriodType(value: String): BudgetPeriodType = BudgetPeriodType.valueOf(value)
    @TypeConverter fun recurrenceFrequency(value: RecurrenceFrequency): String = value.name
    @TypeConverter fun recurrenceFrequency(value: String): RecurrenceFrequency = RecurrenceFrequency.valueOf(value)
    @TypeConverter fun occurrenceStatus(value: RecurrenceOccurrenceStatus): String = value.name
    @TypeConverter fun occurrenceStatus(value: String): RecurrenceOccurrenceStatus = RecurrenceOccurrenceStatus.valueOf(value)
    @TypeConverter fun duePriority(value: DuePriority): String = value.name
    @TypeConverter fun duePriority(value: String): DuePriority = DuePriority.valueOf(value)
}
