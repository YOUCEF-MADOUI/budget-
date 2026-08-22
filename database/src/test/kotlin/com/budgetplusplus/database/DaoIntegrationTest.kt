package com.budgetplusplus.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.budgetplusplus.core.model.*
import com.budgetplusplus.database.entity.*
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35])
class DaoIntegrationTest {
 private lateinit var db:BudgetPlusDatabase
 @Before fun setup()=runBlocking{db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),BudgetPlusDatabase::class.java).allowMainThreadQueries().build();db.openHelper.writableDatabase;createSearchInfrastructure(db.openHelper.writableDatabase);seed()}
 @After fun close(){db.close()}
 private suspend fun seed(){val now=1L;db.openHelper.writableDatabase.execSQL("INSERT INTO workspaces VALUES ('w','Test','PERSONAL','DZD',1,1,1,NULL)");db.accountDao().insert(AccountEntity("a","w","Cash",AccountType.CASH,"DZD",10_000,createdAt=now,updatedAt=now));db.accountDao().insert(AccountEntity("b","w","Bank",AccountType.BANK,"DZD",0,createdAt=now,updatedAt=now));db.categoryDao().insert(CategoryEntity("c","w",CategoryKind.EXPENSE,customName="Food",createdAt=now,updatedAt=now))}
 @Test fun `account balances include expense income and transfer exactly`()=runBlocking{val date="2026-08-22";insert("e",TransactionType.EXPENSE,100,"a",null,"c",date,1);insert("i",TransactionType.INCOME,250,"a",null,null,date,2);insert("t",TransactionType.TRANSFER,300,"a","b",null,date,3);val values=db.accountDao().observeAll().first().associateBy{it.id};assertEquals(9_850,values.getValue("a").currentBalanceMinor);assertEquals(300,values.getValue("b").currentBalanceMinor)}
 @Test fun `budget DAO tracks category expense reactively`()=runBlocking{val now=1L;db.budgetDao().upsert(BudgetEntity("budget","w","Food",BudgetScope.CATEGORY,"c",1000,"DZD",BudgetPeriodType.MONTHLY,"2026-08-01","2026-08-31",createdAt=now,updatedAt=now));db.budgetDao().upsertThreshold(BudgetAlertThresholdEntity("budget",75));insert("e",TransactionType.EXPENSE,400,"a",null,"c","2026-08-22",1);val value=db.budgetDao().observeProgress().first().single();assertEquals(400,value.spentMinor);assertEquals(75,value.warningThresholdPercent)}
 @Test fun `analytics aggregates exclude transfers`()=runBlocking{insert("e",TransactionType.EXPENSE,100,"a",null,"c","2026-08-22",1);insert("i",TransactionType.INCOME,300,"a",null,null,"2026-08-22",2);insert("t",TransactionType.TRANSFER,900,"a","b",null,"2026-08-22",3);val total=db.analyticsDao().observeTotals("2026-08-01","2026-08-31").first();assertEquals(300,total.incomeMinor);assertEquals(100,total.expenseMinor)}
 private suspend fun insert(id:String,type:TransactionType,amount:Long,account:String,dest:String?,category:String?,date:String,time:Long)=db.transactionDao().insert(FinanceTransactionEntity(id,"w",type,amount,"DZD",account,dest,category,null,time,date,"Africa/Algiers",createdAt=time,updatedAt=time))
}
