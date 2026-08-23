package com.budgetplusplus.database

import androidx.sqlite.db.SupportSQLiteDatabase

fun createAccountHierarchyInfrastructure(db: SupportSQLiteDatabase) {
    val cycleCheck = """NEW.parent_account_id = NEW.id OR EXISTS (
        WITH RECURSIVE ancestors(id) AS (
            SELECT NEW.parent_account_id
            UNION ALL
            SELECT a.parent_account_id FROM accounts a JOIN ancestors x ON a.id = x.id WHERE a.parent_account_id IS NOT NULL
        ) SELECT 1 FROM ancestors WHERE id = NEW.id
    )"""
    db.execSQL("CREATE TRIGGER IF NOT EXISTS accounts_parent_cycle_insert BEFORE INSERT ON accounts WHEN NEW.parent_account_id IS NOT NULL BEGIN SELECT CASE WHEN $cycleCheck THEN RAISE(ABORT, 'account_cycle') END; END")
    db.execSQL("CREATE TRIGGER IF NOT EXISTS accounts_parent_cycle_update BEFORE UPDATE OF parent_account_id ON accounts WHEN NEW.parent_account_id IS NOT NULL BEGIN SELECT CASE WHEN $cycleCheck THEN RAISE(ABORT, 'account_cycle') END; END")
}
