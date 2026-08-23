package com.budgetplusplus.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class AnalyticsTotalsRow(val incomeMinor:Long,val expenseMinor:Long)
data class AnalyticsPointRow(val bucket:String,val incomeMinor:Long,val expenseMinor:Long)
data class AnalyticsCategoryRow(val categoryId:String,val nameKey:String?,val customName:String?,val amountMinor:Long)

@Dao interface AnalyticsDao {
 @Query("""SELECT COALESCE(SUM(CASE WHEN type='INCOME' THEN amount_minor ELSE 0 END),0) AS incomeMinor,COALESCE(SUM(CASE WHEN type='EXPENSE' THEN amount_minor ELSE 0 END),0) AS expenseMinor FROM finance_transactions WHERE deleted_at IS NULL AND local_date>=:fromDate AND local_date<=:toDate""")
 fun observeTotals(fromDate:String,toDate:String):Flow<AnalyticsTotalsRow>
 @Query("""SELECT CASE WHEN :monthly=1 THEN substr(local_date,1,7) ELSE local_date END AS bucket,COALESCE(SUM(CASE WHEN type='INCOME' THEN amount_minor ELSE 0 END),0) AS incomeMinor,COALESCE(SUM(CASE WHEN type='EXPENSE' THEN amount_minor ELSE 0 END),0) AS expenseMinor FROM finance_transactions WHERE deleted_at IS NULL AND local_date>=:fromDate AND local_date<=:toDate GROUP BY bucket ORDER BY bucket""")
 fun observeEvolution(fromDate:String,toDate:String,monthly:Boolean):Flow<List<AnalyticsPointRow>>
 @Query("""SELECT c.id AS categoryId,c.name_key AS nameKey,c.custom_name AS customName,SUM(t.amount_minor) AS amountMinor FROM finance_transactions t JOIN categories c ON c.id=t.category_id WHERE t.deleted_at IS NULL AND t.type='EXPENSE' AND t.local_date>=:fromDate AND t.local_date<=:toDate GROUP BY c.id ORDER BY amountMinor DESC""")
 fun observeCategories(fromDate:String,toDate:String):Flow<List<AnalyticsCategoryRow>>
}
