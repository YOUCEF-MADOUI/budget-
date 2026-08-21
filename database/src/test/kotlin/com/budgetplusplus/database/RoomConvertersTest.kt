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
 }
}
