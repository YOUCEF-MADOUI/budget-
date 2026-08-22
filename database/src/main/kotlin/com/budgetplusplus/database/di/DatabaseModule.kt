package com.budgetplusplus.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.budgetplusplus.database.BudgetPlusDatabase
import com.budgetplusplus.database.MIGRATION_1_2
import com.budgetplusplus.database.MIGRATION_2_3
import com.budgetplusplus.database.MIGRATION_3_4
import com.budgetplusplus.database.MIGRATION_4_5
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun database(@ApplicationContext context: Context): BudgetPlusDatabase =
        Room.databaseBuilder(context, BudgetPlusDatabase::class.java, BudgetPlusDatabase.NAME)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val now = System.currentTimeMillis()
                    db.execSQL("INSERT INTO workspaces VALUES (?, ?, ?, ?, 1, ?, ?, NULL)", arrayOf<Any?>(BudgetPlusDatabase.DEFAULT_WORKSPACE_ID, "Budget++", "PERSONAL", "DZD", now, now))
                    val seeds = listOf(
                        Triple("cat-exp-food", "EXPENSE", "category_food"), Triple("cat-exp-transport", "EXPENSE", "category_transport"),
                        Triple("cat-exp-housing", "EXPENSE", "category_housing"), Triple("cat-exp-health", "EXPENSE", "category_health"),
                        Triple("cat-exp-leisure", "EXPENSE", "category_leisure"), Triple("cat-exp-utilities", "EXPENSE", "category_utilities"),
                        Triple("cat-exp-education", "EXPENSE", "category_education"), Triple("cat-exp-family", "EXPENSE", "category_family"),
                        Triple("cat-exp-clothing", "EXPENSE", "category_clothing"), Triple("cat-exp-taxes", "EXPENSE", "category_taxes"),
                        Triple("cat-inc-salary", "INCOME", "category_salary"), Triple("cat-inc-freelance", "INCOME", "category_freelance"),
                        Triple("cat-inc-pension", "INCOME", "category_pension"), Triple("cat-inc-benefits", "INCOME", "category_benefits"),
                        Triple("cat-inc-gift", "INCOME", "category_gift"), Triple("cat-inc-other", "INCOME", "category_other_income"),
                    )
                    seeds.forEachIndexed { index, (id, kind, key) ->
                        db.execSQL("INSERT INTO categories (id, workspace_id, kind, name_key, custom_name, icon_key, color_key, is_system, is_archived, display_order, created_at, updated_at, deleted_at) VALUES (?, ?, ?, ?, NULL, 'category', 'primary', 1, 0, ?, ?, ?, NULL)", arrayOf<Any?>(id, BudgetPlusDatabase.DEFAULT_WORKSPACE_ID, kind, key, index, now, now))
                    }
                }
            }).build()

    @Provides fun accounts(db: BudgetPlusDatabase) = db.accountDao()
    @Provides fun categories(db: BudgetPlusDatabase) = db.categoryDao()
    @Provides fun transactions(db: BudgetPlusDatabase) = db.transactionDao()
    @Provides fun dashboard(db: BudgetPlusDatabase) = db.dashboardDao()
    @Provides fun budgets(db: BudgetPlusDatabase) = db.budgetDao()
    @Provides fun recurring(db: BudgetPlusDatabase) = db.recurringDao()
    @Provides fun analytics(db: BudgetPlusDatabase) = db.analyticsDao()
}
