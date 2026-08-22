package com.budgetplusplus.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.budgetplusplus.core.model.*
import com.budgetplusplus.database.entity.*
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35])
class DaoIntegrationTest {
 private lateinit var db:BudgetPlusDatabase
 @Before fun setup()=runBlocking{db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),BudgetPlusDatabase::class.java).allowMainThreadQueries().build();db.openHelper.writableDatabase;createSearchInfrastructure(db.openHelper.writableDatabase);createAccountHierarchyInfrastructure(db.openHelper.writableDatabase);seed()}
 @After fun close(){db.close()}
 private suspend fun seed(){val now=1L;db.openHelper.writableDatabase.execSQL("INSERT INTO workspaces VALUES ('w','Test','PERSONAL','DZD',1,1,1,NULL)");db.accountDao().insert(AccountEntity("a","w","Cash",AccountType.CASH,"DZD",10_000,createdAt=now,updatedAt=now));db.accountDao().insert(AccountEntity("b","w","Bank",AccountType.BANK,"DZD",0,createdAt=now,updatedAt=now));db.categoryDao().insert(CategoryEntity("c","w",CategoryKind.EXPENSE,customName="Food",createdAt=now,updatedAt=now))}
 @Test fun `account balances include expense income and transfer exactly`()=runBlocking{val date="2026-08-22";insert("e",TransactionType.EXPENSE,100,"a",null,"c",date,1);insert("i",TransactionType.INCOME,250,"a",null,null,date,2);insert("t",TransactionType.TRANSFER,300,"a","b",null,date,3);val values=db.accountDao().observeAll().first().associateBy{it.id};assertEquals(9_850,values.getValue("a").currentBalanceMinor);assertEquals(300,values.getValue("b").currentBalanceMinor);val original=requireNotNull(db.transactionDao().getEntity("e"));db.transactionDao().update(original.copy(type=TransactionType.INCOME,amountMinor=500,accountId="b",categoryId=null,updatedAt=10));val changed=db.accountDao().observeAll().first().associateBy{it.id};assertEquals(9_950,changed.getValue("a").currentBalanceMinor);assertEquals(800,changed.getValue("b").currentBalanceMinor)}
 @Test fun `consolidated balance ignores internal transfers and includes descendants`()=runBlocking{val now=1L;db.accountDao().insert(AccountEntity("root","w","Root",AccountType.BANK,"DZD",0,createdAt=now,updatedAt=now));db.accountDao().insert(AccountEntity("child1","w","Child 1",AccountType.CASH,"DZD",0,createdAt=now,updatedAt=now,parentAccountId="root"));db.accountDao().insert(AccountEntity("child2","w","Child 2",AccountType.CASH,"DZD",0,createdAt=now,updatedAt=now,parentAccountId="root"));insert("income",TransactionType.INCOME,1000,"child1",null,null,"2026-08-22",1);insert("expense",TransactionType.EXPENSE,200,"child2",null,"c","2026-08-22",2);insert("internal",TransactionType.TRANSFER,300,"child1","child2",null,"2026-08-22",3);val metrics=db.accountDao().observeHierarchyMetrics("root").first();assertEquals(0,metrics.ownBalanceMinor);assertEquals(800,metrics.consolidatedBalanceMinor);assertEquals(1000,metrics.consolidatedIncomeMinor);assertEquals(200,metrics.consolidatedExpenseMinor);assertEquals(2,metrics.descendantCount);assertTrue(runCatching{db.accountDao().move("root","child1",4)}.isFailure)}
 @Test fun `media references attach to accounts categories and operations`()=runBlocking{val media=MediaAssetEntity("m","m.webp","m-thumb.webp",100,100,10,1);db.mediaDao().insert(media);insert("e",TransactionType.EXPENSE,100,"a",null,"c","2026-08-22",1);db.mediaDao().attachAccount("a","m",2);db.mediaDao().attachCategory("c","m",2);db.mediaDao().attachTransaction("e","m",2);assertEquals("m",db.accountDao().get("a")?.mediaId);assertEquals("m",db.categoryDao().observeDetails().first().single().mediaId);assertEquals("m",db.transactionDao().getEntity("e")?.mediaId)}
 @Test fun `budget DAO tracks category expense reactively`()=runBlocking{val now=1L;db.budgetDao().upsert(BudgetEntity("budget","w","Food",BudgetScope.CATEGORY,"c",1000,"DZD",BudgetPeriodType.MONTHLY,"2026-08-01","2026-08-31",createdAt=now,updatedAt=now));db.budgetDao().upsertThreshold(BudgetAlertThresholdEntity("budget",75));insert("e",TransactionType.EXPENSE,400,"a",null,"c","2026-08-22",1);val value=db.budgetDao().observeProgress().first().single();assertEquals(400,value.spentMinor);assertEquals(75,value.warningThresholdPercent)}
 @Test fun `analytics aggregates exclude transfers`()=runBlocking{insert("e",TransactionType.EXPENSE,100,"a",null,"c","2026-08-22",1);insert("i",TransactionType.INCOME,300,"a",null,null,"2026-08-22",2);insert("t",TransactionType.TRANSFER,900,"a","b",null,"2026-08-22",3);val total=db.analyticsDao().observeTotals("2026-08-01","2026-08-31").first();assertEquals(300,total.incomeMinor);assertEquals(100,total.expenseMinor)}
 private suspend fun insert(id:String,type:TransactionType,amount:Long,account:String,dest:String?,category:String?,date:String,time:Long)=db.transactionDao().insert(FinanceTransactionEntity(id,"w",type,amount,"DZD",account,dest,category,null,time,date,"Africa/Algiers",createdAt=time,updatedAt=time))
}
