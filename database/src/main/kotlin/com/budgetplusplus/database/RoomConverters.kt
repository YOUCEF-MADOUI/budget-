package com.budgetplusplus.database

import androidx.room.TypeConverter
import com.budgetplusplus.core.model.AccountType
import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.core.model.TransactionType

class RoomConverters {
    @TypeConverter fun accountType(value: AccountType): String = value.name
    @TypeConverter fun accountType(value: String): AccountType = AccountType.valueOf(value)
    @TypeConverter fun categoryKind(value: CategoryKind): String = value.name
    @TypeConverter fun categoryKind(value: String): CategoryKind = CategoryKind.valueOf(value)
    @TypeConverter fun transactionType(value: TransactionType): String = value.name
    @TypeConverter fun transactionType(value: String): TransactionType = TransactionType.valueOf(value)
}
