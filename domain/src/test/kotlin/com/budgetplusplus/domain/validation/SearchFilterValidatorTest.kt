package com.budgetplusplus.domain.validation
import com.budgetplusplus.core.model.TransactionSearchFilter
import org.junit.Assert.*
import org.junit.Test
class SearchFilterValidatorTest{
 @Test fun `accepts combined valid bounds`(){assertTrue(SearchFilterValidator.isValid(TransactionSearchFilter(fromDate="2026-01-01",toDate="2026-12-31",minimumMinor=100,maximumMinor=1000)))}
 @Test fun `rejects reversed dates and amounts`(){assertFalse(SearchFilterValidator.isValid(TransactionSearchFilter(fromDate="2026-02-01",toDate="2026-01-01")));assertFalse(SearchFilterValidator.isValid(TransactionSearchFilter(minimumMinor=2,maximumMinor=1)))}
}
