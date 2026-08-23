package com.budgetplusplus.database

import androidx.sqlite.db.SupportSQLiteDatabase

fun createSearchInfrastructure(db:SupportSQLiteDatabase){
 db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS finance_transactions_fts USING fts4(transaction_id TEXT, description TEXT, tokenize=unicode61)")
 db.execSQL("INSERT INTO finance_transactions_fts(transaction_id,description) SELECT id,description FROM finance_transactions WHERE id NOT IN (SELECT transaction_id FROM finance_transactions_fts)")
 db.execSQL("CREATE TRIGGER IF NOT EXISTS finance_transactions_fts_ai AFTER INSERT ON finance_transactions BEGIN INSERT INTO finance_transactions_fts(transaction_id,description) VALUES(new.id,new.description); END")
 db.execSQL("CREATE TRIGGER IF NOT EXISTS finance_transactions_fts_au AFTER UPDATE OF description ON finance_transactions BEGIN DELETE FROM finance_transactions_fts WHERE transaction_id=old.id; INSERT INTO finance_transactions_fts(transaction_id,description) VALUES(new.id,new.description); END")
 db.execSQL("CREATE TRIGGER IF NOT EXISTS finance_transactions_fts_ad AFTER DELETE ON finance_transactions BEGIN DELETE FROM finance_transactions_fts WHERE transaction_id=old.id; END")
}
