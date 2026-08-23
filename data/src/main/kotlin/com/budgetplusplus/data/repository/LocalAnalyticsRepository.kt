package com.budgetplusplus.data.repository

import com.budgetplusplus.core.model.*
import com.budgetplusplus.database.dao.AnalyticsDao
import com.budgetplusplus.domain.analytics.AnalyticsCalculator
import com.budgetplusplus.domain.repository.AnalyticsRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class LocalAnalyticsRepository @Inject constructor(private val dao:AnalyticsDao):AnalyticsRepository{
 override fun observeAnalytics(fromDate:String,toDate:String,previousFromDate:String,previousToDate:String,monthlyBuckets:Boolean):Flow<AnalyticsData>{
  val from=LocalDate.parse(fromDate);val to=LocalDate.parse(toDate);val days=AnalyticsCalculator.inclusiveDays(from,to);val months=AnalyticsCalculator.inclusiveMonths(from,to)
  return combine(dao.observeTotals(fromDate,toDate),dao.observeTotals(previousFromDate,previousToDate),dao.observeEvolution(fromDate,toDate,monthlyBuckets),dao.observeCategories(fromDate,toDate)){current,previous,points,categories->
   AnalyticsData(AnalyticsTotals(current.incomeMinor,current.expenseMinor),AnalyticsTotals(previous.incomeMinor,previous.expenseMinor),points.map{AnalyticsPoint(it.bucket,it.incomeMinor,it.expenseMinor)},categories.map{AnalyticsCategory(it.categoryId,it.nameKey,it.customName,it.amountMinor)},AnalyticsCalculator.roundedAverage(current.incomeMinor,days),AnalyticsCalculator.roundedAverage(current.expenseMinor,days),AnalyticsCalculator.roundedAverage(current.incomeMinor,months),AnalyticsCalculator.roundedAverage(current.expenseMinor,months))
  }
 }
}
