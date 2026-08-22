package com.budgetplusplus.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS index_finance_transactions_occurred_at_type_deleted_at ON finance_transactions (occurred_at, type, deleted_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_finance_transactions_category_id_occurred_at_deleted_at ON finance_transactions (category_id, occurred_at, deleted_at)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS subcategories (
            id TEXT NOT NULL PRIMARY KEY, category_id TEXT NOT NULL, name_key TEXT, custom_name TEXT,
            is_system INTEGER NOT NULL, is_archived INTEGER NOT NULL, display_order INTEGER NOT NULL,
            created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, deleted_at INTEGER,
            FOREIGN KEY(category_id) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_subcategories_category_id_is_archived_deleted_at ON subcategories (category_id, is_archived, deleted_at)")
        db.execSQL("ALTER TABLE finance_transactions ADD COLUMN subcategory_id TEXT REFERENCES subcategories(id) ON UPDATE NO ACTION ON DELETE SET NULL")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_finance_transactions_subcategory_id ON finance_transactions (subcategory_id)")
        val now = System.currentTimeMillis()
        val seeds = listOf(
            Triple("cat-exp-utilities", "EXPENSE", "category_utilities"), Triple("cat-exp-education", "EXPENSE", "category_education"),
            Triple("cat-exp-family", "EXPENSE", "category_family"), Triple("cat-exp-clothing", "EXPENSE", "category_clothing"),
            Triple("cat-exp-taxes", "EXPENSE", "category_taxes"), Triple("cat-inc-freelance", "INCOME", "category_freelance"),
            Triple("cat-inc-pension", "INCOME", "category_pension"), Triple("cat-inc-benefits", "INCOME", "category_benefits"),
        )
        seeds.forEachIndexed { index, (id, kind, key) ->
            db.execSQL("INSERT OR IGNORE INTO categories (id, workspace_id, kind, name_key, custom_name, icon_key, color_key, is_system, is_archived, display_order, created_at, updated_at, deleted_at) VALUES (?, ?, ?, ?, NULL, 'category', 'primary', 1, 0, ?, ?, ?, NULL)", arrayOf<Any?>(id, BudgetPlusDatabase.DEFAULT_WORKSPACE_ID, kind, key, index + 20, now, now))
        }
    }
}
