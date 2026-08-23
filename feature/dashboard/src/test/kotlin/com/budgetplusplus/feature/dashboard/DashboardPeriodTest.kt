package com.budgetplusplus.feature.dashboard

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardPeriodTest {
    private val today = LocalDate.of(2026, 8, 22)
    @Test fun `week starts on Monday and ends today`() {
        assertEquals(DateInterval(LocalDate.of(2026, 8, 17), today), DashboardViewModel.defaultInterval(DashboardPeriod.WEEK, today))
    }
    @Test fun `month and year start at calendar boundaries`() {
        assertEquals(LocalDate.of(2026, 8, 1), DashboardViewModel.defaultInterval(DashboardPeriod.MONTH, today).from)
        assertEquals(LocalDate.of(2026, 1, 1), DashboardViewModel.defaultInterval(DashboardPeriod.YEAR, today).from)
    }
}
