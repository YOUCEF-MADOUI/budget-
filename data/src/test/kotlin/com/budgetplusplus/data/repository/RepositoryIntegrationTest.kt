package com.budgetplusplus.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.budgetplusplus.core.model.*
import com.budgetplusplus.database.*
import com.budgetplusplus.database.entity.WorkspaceEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35])
class RepositoryIntegrationTest {
 private lateinit var db:BudgetPlusDatabase;private lateinit var accounts:LocalAccountRepository;private lateinit var categories:LocalCategoryRepository;private lateinit var transactions:LocalTransactionRepository
 @Before fun setup()=runBlocking{db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),BudgetPlusDatabase::class.java).allowMainThreadQueries().build();db.openHelper.writableDatabase;createSearchInfrastructure(db.openHelper.writableDatabase);val now=1L;db.openHelper.writableDatabase.execSQL("INSERT INTO workspaces VALUES ('${BudgetPlusDatabase.DEFAULT_WORKSPACE_ID}','Test','PERSONAL','DZD',1,$now,$now,NULL)");accounts=LocalAccountRepository(db.accountDao());categories=LocalCategoryRepository(db,db.categoryDao());transactions=LocalTransactionRepository(db,db.transactionDao(),db.accountDao(),db.categoryDao())}
 @After fun close(){db.close()}
 @Test fun `account expense income and transfer journey updates exact balances`()=runBlocking{accounts.create("Cash",AccountType.CASH,10_000);accounts.create("Bank",AccountType.BANK,0);categories.create("Food",CategoryKind.EXPENSE);categories.create("Salary",CategoryKind.INCOME);val a=accounts.observeAccounts().first().associateBy{it.name};val cats=categories.observeCategories().first().associateBy{it.customName};transactions.create(TransactionType.EXPENSE,1_000,a.getValue("Cash").id,null,cats.getValue("Food").id,"Lunch");transactions.create(TransactionType.INCOME,2_000,a.getValue("Cash").id,null,cats.getValue("Salary").id,"Pay");transactions.create(TransactionType.TRANSFER,500,a.getValue("Cash").id,a.getValue("Bank").id,null,"Move");val balances=accounts.observeAccounts().first().associateBy{it.name};assertEquals(10_500,balances.getValue("Cash").currentBalanceMinor);assertEquals(500,balances.getValue("Bank").currentBalanceMinor);assertEquals(3,transactions.observeTransactions().first().size)}
 @Test fun `budget repository follows an expense in real time`()=runBlocking{accounts.create("Cash",AccountType.CASH,0);categories.create("Food",CategoryKind.EXPENSE);val account=accounts.observeAccounts().first().single();val category=categories.observeCategories().first().single();val budgets=LocalBudgetRepository(db,db.budgetDao());val today=LocalDate.now();budgets.save(BudgetInput(name="Food",scope=BudgetScope.CATEGORY,categoryId=category.id,amountMinor=10_000,periodType=BudgetPeriodType.CUSTOM,startDate=today.minusDays(1).toString(),endDate=today.plusDays(1).toString()));transactions.create(TransactionType.EXPENSE,2_500,account.id,null,category.id,"Food");assertEquals(2_500,budgets.observeBudgets().first().single().spentMinor)}
}
