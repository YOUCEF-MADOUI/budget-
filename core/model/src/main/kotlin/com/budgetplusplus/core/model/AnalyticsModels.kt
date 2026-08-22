package com.budgetplusplus.core.model

data class AnalyticsTotals(val incomeMinor:Long=0,val expenseMinor:Long=0){val netMinor:Long get()=Math.subtractExact(incomeMinor,expenseMinor)}
data class AnalyticsPoint(val bucket:String,val incomeMinor:Long,val expenseMinor:Long){val netMinor:Long get()=Math.subtractExact(incomeMinor,expenseMinor)}
data class AnalyticsCategory(val categoryId:String,val nameKey:String?,val customName:String?,val amountMinor:Long)
data class AnalyticsData(
 val current:AnalyticsTotals=AnalyticsTotals(),
 val previous:AnalyticsTotals=AnalyticsTotals(),
 val evolution:List<AnalyticsPoint> = emptyList(),
 val categories:List<AnalyticsCategory> = emptyList(),
 val dailyAverageIncomeMinor:Long=0,
 val dailyAverageExpenseMinor:Long=0,
 val monthlyAverageIncomeMinor:Long=0,
 val monthlyAverageExpenseMinor:Long=0,
 val currencyCode:String="DZD",
)
