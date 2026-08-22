package com.budgetplusplus.feature.accounts

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.budgetplusplus.core.designsystem.theme.BudgetPlusPlusTheme
import com.budgetplusplus.core.model.*
import com.budgetplusplus.domain.repository.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35],qualifiers="fr")
class AccountDetailScreenTest {
 @get:Rule val compose=createComposeRule();private val accounts=DetailAccounts();private val search=DetailSearch();private lateinit var viewModel:AccountDetailViewModel
 @Before fun setup(){viewModel=AccountDetailViewModel(accounts,search);compose.setContent{BudgetPlusPlusTheme{AccountDetailScreen("a",{}, {}, {_,_->},{},viewModel)}}}
 @Test fun `detail renders account and exposes quick action plus reassignment`(){compose.waitUntil{search.calls>0};compose.waitUntil{compose.onAllNodesWithText("Cash").fetchSemanticsNodes().isNotEmpty()};compose.onNodeWithText("Ajouter une dépense").assertExists();compose.runOnIdle{viewModel.toggle("tx");viewModel.reassign("b"){}};compose.waitUntil{accounts.moved!=null};assertEquals(setOf("tx"),accounts.moved?.third)}
}
private class DetailAccounts:AccountRepository{val list=flowOf(listOf(Account("a","Cash",AccountType.CASH,"DZD",1000,900),Account("b","Bank",AccountType.BANK,"DZD",0,0)));var moved:Triple<String,String,Set<String>>?=null;override fun observeAccounts()=list;override fun observeTotals(id:String)=flowOf(AccountOperationTotals(0,100,1));override fun observeHierarchyMetrics(id:String)=flowOf(AccountHierarchyMetrics(900,900,0,100,0,100,0));override suspend fun create(name:String,type:AccountType,initialBalanceMinor:Long,currencyCode:String,parentAccountId:String?)=Unit;override suspend fun update(id:String,name:String,type:AccountType,iconKey:String,colorKey:String,description:String,displayOrder:Int)=Unit;override suspend fun move(id:String,parentAccountId:String?)=Unit;override suspend fun setArchived(id:String,archived:Boolean)=Unit;override suspend fun reassignOperations(sourceAccountId:String,targetAccountId:String,transactionIds:Set<String>){moved=Triple(sourceAccountId,targetAccountId,transactionIds)};override suspend fun deleteAndReassign(sourceAccountId:String,targetAccountId:String?)=Unit}
private class DetailSearch:SearchRepository{var calls=0;override suspend fun search(filter:TransactionSearchFilter,cursor:SearchCursor?,pageSize:Int):TransactionSearchPage{calls++;return TransactionSearchPage(listOf(FinanceTransaction("tx",TransactionType.EXPENSE,100,"DZD","a","Cash",categoryCustomName="Expense",occurredAt=1)),null)}}
