package com.budgetplusplus.domain.analytics

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

object AnalyticsCalculator {
 fun roundedAverage(totalMinor:Long,count:Long):Long=if(count<=0)0 else BigDecimal.valueOf(totalMinor).divide(BigDecimal.valueOf(count),0,RoundingMode.HALF_UP).longValueExact()
 fun inclusiveDays(from:LocalDate,to:LocalDate):Long=ChronoUnit.DAYS.between(from,to)+1
 fun inclusiveMonths(from:LocalDate,to:LocalDate):Long=ChronoUnit.MONTHS.between(YearMonth.from(from),YearMonth.from(to))+1
}
