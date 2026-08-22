package com.budgetplusplus.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.budgetplusplus.database.dao.AccountDao
import com.budgetplusplus.database.dao.AnalyticsDao
import com.budgetplusplus.database.dao.CategoryDao
import com.budgetplusplus.database.dao.BudgetDao
import com.budgetplusplus.database.dao.DashboardDao
import com.budgetplusplus.database.dao.TransactionDao
import com.budgetplusplus.database.dao.SearchDao
import com.budgetplusplus.database.dao.RecurringDao
import com.budgetplusplus.database.entity.AccountEntity
import com.budgetplusplus.database.entity.BudgetAlertEventEntity
import com.budgetplusplus.database.entity.BudgetAlertThresholdEntity
import com.budgetplusplus.database.entity.BudgetEntity
import com.budgetplusplus.database.entity.CategoryEntity
import com.budgetplusplus.database.entity.FinanceTransactionEntity
import com.budgetplusplus.database.entity.SubcategoryEntity
import com.budgetplusplus.database.entity.WorkspaceEntity
import com.budgetplusplus.database.entity.RecurringTransactionEntity
import com.budgetplusplus.database.entity.RecurringOccurrenceEntity

@Database(entities = [WorkspaceEntity::class, AccountEntity::class, CategoryEntity::class, SubcategoryEntity::class, FinanceTransactionEntity::class, BudgetEntity::class, BudgetAlertThresholdEntity::class, BudgetAlertEventEntity::class, RecurringTransactionEntity::class, RecurringOccurrenceEntity::class], version = 6, exportSchema = true)
@TypeConverters(RoomConverters::class)
abstract class BudgetPlusDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun dashboardDao(): DashboardDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringDao(): RecurringDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun searchDao(): SearchDao

    companion object {
        const val NAME = "budget_plus_plus.db"
        const val SCHEMA_VERSION = 6
        const val DEFAULT_WORKSPACE_ID = "00000000-0000-0000-0000-000000000001"
    }
}
