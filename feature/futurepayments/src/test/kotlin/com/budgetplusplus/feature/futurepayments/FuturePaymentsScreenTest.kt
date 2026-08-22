package com.budgetplusplus.feature.futurepayments

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.budgetplusplus.core.designsystem.theme.BudgetPlusPlusTheme
import com.budgetplusplus.core.model.*
import com.budgetplusplus.domain.repository.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35],qualifiers="fr")
class FuturePaymentsScreenTest{@get:Rule val compose=createComposeRule();private val repo=FakeDues();private lateinit var vm:FuturePaymentsViewModel
 @Before fun setup(){vm=FuturePaymentsViewModel(repo,EmptyDueAccounts(),EmptyDueCategories());compose.setContent{BudgetPlusPlusTheme{FuturePaymentsScreen({},vm)}}}
 @Test fun `empty state and upcoming due are accessible`(){compose.onNodeWithText("Aucune échéance").assertExists();repo.values.value=listOf(FuturePayment("d","Facture","",1000,0,"DZD","2099-01-01","c",null,"Factures",null,null,null,null,DuePriority.HIGH,false,1,"",false,DueStatus.UPCOMING));compose.waitUntil{compose.onAllNodesWithText("Facture").fetchSemanticsNodes().isNotEmpty()};compose.onNodeWithText("À venir").assertExists();compose.onNodeWithText("Payer").assertExists()}}
private class FakeDues:FuturePaymentRepository{val values=MutableStateFlow<List<FuturePayment>>(emptyList());override fun observeFuturePayments()=values;override fun observePayments(dueId:String)=flowOf(emptyList<DuePayment>());override suspend fun save(input:FuturePaymentInput)=Unit;override suspend fun setCancelled(id:String,cancelled:Boolean)=Unit;override suspend fun pay(dueId:String,requestId:String,accountId:String,amountMinor:Long,paidDate:String)="tx";override suspend fun cancelPayment(paymentId:String)=Unit;override suspend fun claimReminders(today:String)=emptyList<DueReminder>()}
private class EmptyDueAccounts:AccountRepository{override fun observeAccounts()=flowOf(emptyList<Account>());override fun observeTotals(id:String)=flowOf(AccountOperationTotals());override fun observeHierarchyMetrics(id:String)=flowOf(AccountHierarchyMetrics());override suspend fun create(name:String,type:AccountType,initialBalanceMinor:Long,currencyCode:String,parentAccountId:String?)=Unit;override suspend fun update(id:String,name:String,type:AccountType,iconKey:String,colorKey:String,description:String,displayOrder:Int)=Unit;override suspend fun move(id:String,parentAccountId:String?)=Unit;override suspend fun setArchived(id:String,archived:Boolean)=Unit;override suspend fun reassignOperations(sourceAccountId:String,targetAccountId:String,transactionIds:Set<String>)=Unit;override suspend fun deleteAndReassign(sourceAccountId:String,targetAccountId:String?)=Unit}
private class EmptyDueCategories:CategoryRepository{override fun observeCategories(kind:CategoryKind?)=flowOf(emptyList<Category>());override fun observeSubcategories()=flowOf(emptyList<Subcategory>());override suspend fun create(name:String,kind:CategoryKind,iconKey:String,colorKey:String)="";override suspend fun update(id:String,name:String,iconKey:String,colorKey:String)=Unit;override suspend fun createSubcategory(categoryId:String,name:String)="";override suspend fun updateSubcategory(id:String,name:String)=Unit;override suspend fun setArchived(id:String,archived:Boolean,replacementId:String?)=Unit;override suspend fun setSubcategoryArchived(id:String,archived:Boolean,replacementId:String?)=Unit;override suspend fun delete(id:String,replacementId:String?)=Unit}
