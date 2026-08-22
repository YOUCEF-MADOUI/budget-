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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS budgets (
            id TEXT NOT NULL PRIMARY KEY, workspace_id TEXT NOT NULL, name TEXT NOT NULL, scope TEXT NOT NULL,
            category_id TEXT, amount_minor INTEGER NOT NULL, currency_code TEXT NOT NULL, period_type TEXT NOT NULL,
            start_date TEXT NOT NULL, end_date TEXT NOT NULL, is_archived INTEGER NOT NULL,
            created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, deleted_at INTEGER,
            FOREIGN KEY(workspace_id) REFERENCES workspaces(id) ON UPDATE NO ACTION ON DELETE CASCADE,
            FOREIGN KEY(category_id) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_workspace_id_is_archived_start_date_deleted_at ON budgets (workspace_id, is_archived, start_date, deleted_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_category_id ON budgets (category_id)")
        db.execSQL("""CREATE TABLE IF NOT EXISTS budget_alert_thresholds (
            budget_id TEXT NOT NULL, percentage INTEGER NOT NULL, enabled INTEGER NOT NULL,
            PRIMARY KEY(budget_id, percentage),
            FOREIGN KEY(budget_id) REFERENCES budgets(id) ON UPDATE NO ACTION ON DELETE CASCADE)""")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_budget_alert_thresholds_budget_id ON budget_alert_thresholds (budget_id)")
        db.execSQL("""CREATE TABLE IF NOT EXISTS budget_alert_events (
            id TEXT NOT NULL PRIMARY KEY, budget_id TEXT NOT NULL, period_key TEXT NOT NULL,
            percentage INTEGER NOT NULL, notified_at INTEGER NOT NULL,
            FOREIGN KEY(budget_id) REFERENCES budgets(id) ON UPDATE NO ACTION ON DELETE CASCADE)""")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_budget_alert_events_budget_id_period_key_percentage ON budget_alert_events (budget_id, period_key, percentage)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
 override fun migrate(db:SupportSQLiteDatabase){
  db.execSQL("""CREATE TABLE IF NOT EXISTS recurring_transactions (id TEXT NOT NULL PRIMARY KEY,workspace_id TEXT NOT NULL,name TEXT NOT NULL,type TEXT NOT NULL,amount_minor INTEGER NOT NULL,currency_code TEXT NOT NULL,account_id TEXT NOT NULL,destination_account_id TEXT,category_id TEXT,subcategory_id TEXT,description TEXT NOT NULL,frequency TEXT NOT NULL,anchor_month INTEGER NOT NULL,anchor_day INTEGER NOT NULL,start_date TEXT NOT NULL,end_date TEXT,next_due_date TEXT NOT NULL,zone_id TEXT NOT NULL,is_active INTEGER NOT NULL,created_at INTEGER NOT NULL,updated_at INTEGER NOT NULL,deleted_at INTEGER,FOREIGN KEY(workspace_id) REFERENCES workspaces(id) ON UPDATE NO ACTION ON DELETE CASCADE,FOREIGN KEY(account_id) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT,FOREIGN KEY(destination_account_id) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT,FOREIGN KEY(category_id) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE SET NULL,FOREIGN KEY(subcategory_id) REFERENCES subcategories(id) ON UPDATE NO ACTION ON DELETE SET NULL)""")
  db.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_workspace_id_is_active_next_due_date_deleted_at ON recurring_transactions(workspace_id,is_active,next_due_date,deleted_at)")
  listOf("account_id","destination_account_id","category_id","subcategory_id").forEach{db.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_${it} ON recurring_transactions(${it})")}
  db.execSQL("""CREATE TABLE IF NOT EXISTS recurring_occurrences (id TEXT NOT NULL PRIMARY KEY,recurring_transaction_id TEXT NOT NULL,due_date TEXT NOT NULL,status TEXT NOT NULL,transaction_id TEXT,error_code TEXT,processed_at INTEGER NOT NULL,FOREIGN KEY(recurring_transaction_id) REFERENCES recurring_transactions(id) ON UPDATE NO ACTION ON DELETE CASCADE)""")
  db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_recurring_occurrences_recurring_transaction_id_due_date ON recurring_occurrences(recurring_transaction_id,due_date)")
  db.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_occurrences_transaction_id ON recurring_occurrences(transaction_id)")
 }
}

val MIGRATION_5_6 = object : Migration(5,6){override fun migrate(db:SupportSQLiteDatabase){createSearchInfrastructure(db);db.execSQL("CREATE INDEX IF NOT EXISTS index_finance_transactions_amount_minor_occurred_at_deleted_at ON finance_transactions(amount_minor,occurred_at,deleted_at)")}}

val MIGRATION_6_7 = object : Migration(6,7){override fun migrate(db:SupportSQLiteDatabase){db.execSQL("CREATE INDEX IF NOT EXISTS index_finance_transactions_deleted_at_occurred_at_id ON finance_transactions(deleted_at,occurred_at,id)");db.execSQL("CREATE INDEX IF NOT EXISTS index_finance_transactions_deleted_at_amount_minor_id ON finance_transactions(deleted_at,amount_minor,id)");db.execSQL("CREATE INDEX IF NOT EXISTS index_finance_transactions_deleted_at_local_date_type_category_id ON finance_transactions(deleted_at,local_date,type,category_id)")}}
