package com.budgetplusplus.data.search
import com.budgetplusplus.core.model.*
import org.junit.Assert.*
import org.junit.Test
class TransactionSearchQueryFactoryTest{
 @Test fun `combines filters with indexed keyset pagination`(){val q=TransactionSearchQueryFactory.build(TransactionSearchFilter(text="café",type=TransactionType.EXPENSE,accountId="a",categoryId="c",minimumMinor=10,sort=TransactionSort.AMOUNT_DESC),SearchCursor(500,"id"),51);assertTrue(q.sql.contains("description MATCH"));assertTrue(q.sql.contains("t.type=?"));assertTrue(q.sql.contains("t.amount_minor<?"));assertTrue(q.sql.contains("ORDER BY t.amount_minor DESC"));assertFalse(q.sql.contains("OFFSET"))}
 @Test fun `escapes full text input as prefix terms`(){assertEquals("\"hello\"* AND \"world\"*",TransactionSearchQueryFactory.matchExpression("hello world"))}
}
