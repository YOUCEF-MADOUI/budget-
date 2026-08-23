package com.budgetplusplus.domain.recurring
import com.budgetplusplus.core.model.RecurrenceFrequency
import java.time.*
import org.junit.Assert.*
import org.junit.Test
class RecurrenceCalculatorTest {
 @Test fun `monthly recurrence preserves anchor across short months`() { val feb=RecurrenceCalculator.nextAfter(LocalDate.of(2026,1,31),RecurrenceFrequency.MONTHLY,1,31);assertEquals(LocalDate.of(2026,2,28),feb);assertEquals(LocalDate.of(2026,3,31),RecurrenceCalculator.nextAfter(feb,RecurrenceFrequency.MONTHLY,1,31)) }
 @Test fun `leap day yearly recurrence clamps safely`() { assertEquals(LocalDate.of(2025,2,28),RecurrenceCalculator.nextAfter(LocalDate.of(2024,2,29),RecurrenceFrequency.YEARLY,2,29)) }
 @Test fun `zoned execution resolves daylight saving gap`() { val zone=ZoneId.of("Europe/Paris");val value=LocalDate.of(2026,3,29).atTime(2,30).atZone(zone);assertEquals(3,value.hour) }
 @Test fun `occurrence keys are deterministic`() { val date=LocalDate.of(2026,8,22);assertEquals(OccurrenceKey.id("rule",date),OccurrenceKey.id("rule",date));assertNotEquals(OccurrenceKey.id("rule",date),OccurrenceKey.id("rule",date.plusDays(1))) }
}
