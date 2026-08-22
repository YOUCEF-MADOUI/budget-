package com.budgetplusplus.database
import com.budgetplusplus.core.model.*
import org.junit.Assert.assertEquals
import org.junit.Test
class RoomConvertersTest {
 private val converter = RoomConverters()
 @Test fun `financial enums round trip as stable names`() {
  assertEquals(AccountType.SAVINGS, converter.accountType(converter.accountType(AccountType.SAVINGS)))
  assertEquals(CategoryKind.EXPENSE, converter.categoryKind(converter.categoryKind(CategoryKind.EXPENSE)))
  assertEquals(TransactionType.TRANSFER, converter.transactionType(converter.transactionType(TransactionType.TRANSFER)))
  assertEquals(BudgetScope.CATEGORY, converter.budgetScope(converter.budgetScope(BudgetScope.CATEGORY)))
  assertEquals(BudgetPeriodType.CUSTOM, converter.budgetPeriodType(converter.budgetPeriodType(BudgetPeriodType.CUSTOM)))
  assertEquals(RecurrenceFrequency.MONTHLY, converter.recurrenceFrequency(converter.recurrenceFrequency(RecurrenceFrequency.MONTHLY)))
  assertEquals(FavoriteBehavior.IMMEDIATE, converter.favoriteBehavior(converter.favoriteBehavior(FavoriteBehavior.IMMEDIATE)))
 }
}
