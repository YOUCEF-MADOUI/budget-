package com.budgetplusplus.domain.analytics

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsCalculatorTest {
 @Test fun `daily average rounds minor units without floating point`(){assertEquals(333L,AnalyticsCalculator.roundedAverage(1000L,3))}
 @Test fun `inclusive day count includes both bounds`(){assertEquals(7L,AnalyticsCalculator.inclusiveDays(LocalDate.of(2026,8,1),LocalDate.of(2026,8,7)))}
 @Test fun `monthly count crosses year safely`(){assertEquals(3L,AnalyticsCalculator.inclusiveMonths(LocalDate.of(2025,12,15),LocalDate.of(2026,2,2)))}
}
