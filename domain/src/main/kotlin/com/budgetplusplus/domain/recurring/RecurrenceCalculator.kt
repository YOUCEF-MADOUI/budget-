package com.budgetplusplus.domain.recurring

import com.budgetplusplus.core.model.RecurrenceFrequency
import java.time.LocalDate
import java.time.YearMonth

object RecurrenceCalculator {
    fun nextAfter(current: LocalDate, frequency: RecurrenceFrequency, anchorMonth: Int, anchorDay: Int): LocalDate = when (frequency) {
        RecurrenceFrequency.DAILY -> current.plusDays(1)
        RecurrenceFrequency.WEEKLY -> current.plusWeeks(1)
        RecurrenceFrequency.MONTHLY -> YearMonth.from(current).plusMonths(1).let { it.atDay(anchorDay.coerceAtMost(it.lengthOfMonth())) }
        RecurrenceFrequency.YEARLY -> {
            val year = current.year + 1
            val month = YearMonth.of(year, anchorMonth)
            month.atDay(anchorDay.coerceAtMost(month.lengthOfMonth()))
        }
    }
}
