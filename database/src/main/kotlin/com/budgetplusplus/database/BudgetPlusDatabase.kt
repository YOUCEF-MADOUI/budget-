package com.budgetplusplus.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.budgetplusplus.database.dao.AccountDao
import com.budgetplusplus.database.dao.CategoryDao
import com.budgetplusplus.database.dao.DashboardDao
import com.budgetplusplus.database.dao.TransactionDao
import com.budgetplusplus.database.entity.AccountEntity
import com.budgetplusplus.database.entity.CategoryEntity
import com.budgetplusplus.database.entity.FinanceTransactionEntity
import com.budgetplusplus.database.entity.SubcategoryEntity
import com.budgetplusplus.database.entity.WorkspaceEntity

@Database(entities = [WorkspaceEntity::class, AccountEntity::class, CategoryEntity::class, SubcategoryEntity::class, FinanceTransactionEntity::class], version = 3, exportSchema = true)
@TypeConverters(RoomConverters::class)
abstract class BudgetPlusDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun dashboardDao(): DashboardDao

    companion object {
        const val NAME = "budget_plus_plus.db"
        const val DEFAULT_WORKSPACE_ID = "00000000-0000-0000-0000-000000000001"
    }
}
