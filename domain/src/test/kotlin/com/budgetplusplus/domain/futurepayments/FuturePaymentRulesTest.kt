package com.budgetplusplus.domain.futurepayments

import com.budgetplusplus.core.model.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class FuturePaymentRulesTest{
 private val today=LocalDate.of(2026,8,22)
 @Test fun `derives upcoming today overdue partial paid and cancelled states`(){assertEquals(DueStatus.UPCOMING,FuturePaymentRules.status(today.plusDays(1),100,0,false,today));assertEquals(DueStatus.TODAY,FuturePaymentRules.status(today,100,0,false,today));assertEquals(DueStatus.OVERDUE,FuturePaymentRules.status(today.minusDays(1),100,0,false,today));assertEquals(DueStatus.PARTIALLY_PAID,FuturePaymentRules.status(today.minusDays(1),100,20,false,today));assertEquals(DueStatus.PAID,FuturePaymentRules.status(today,100,100,false,today));assertEquals(DueStatus.CANCELLED,FuturePaymentRules.status(today,100,100,true,today))}
 @Test fun `payment validation prevents overpayment and invalid dates`(){assertTrue(FuturePaymentRules.validPayment(50,100,"2026-08-22"));assertFalse(FuturePaymentRules.validPayment(101,100,"2026-08-22"));assertFalse(FuturePaymentRules.validPayment(50,100,"invalid"))}
}
