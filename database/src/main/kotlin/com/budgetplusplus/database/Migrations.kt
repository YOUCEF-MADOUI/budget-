package com.budgetplusplus.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS index_finance_transactions_occurred_at_type_deleted_at ON finance_transactions (occurred_at, type, deleted_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_finance_transactions_category_id_occurred_at_deleted_at ON finance_transactions (category_id, occurred_at, deleted_at)")
    }
}
