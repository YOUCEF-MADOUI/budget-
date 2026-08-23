package com.budgetplusplus.database.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.budgetplusplus.database.entity.FinanceTransactionEntity

@Dao interface SearchDao {
 @RawQuery(observedEntities=[FinanceTransactionEntity::class])
 suspend fun search(query:SupportSQLiteQuery):List<TransactionDetails>
}
