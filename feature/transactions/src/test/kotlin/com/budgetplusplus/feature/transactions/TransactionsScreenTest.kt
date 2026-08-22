package com.budgetplusplus.feature.transactions

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
class TransactionsScreenTest {
 @get:Rule val compose=createComposeRule();private val transactions=FakeTransactions();private val accounts=FakeTransactionAccounts();private val categories=FakeTransactionCategories();private lateinit var viewModel:TransactionsViewModel
 @Before fun content(){viewModel=TransactionsViewModel(accounts,categories,transactions);compose.setContent{BudgetPlusPlusTheme{TransactionsScreen(null,{},viewModel)}}}
 @Test fun `expense journey selects account category and exact amount`(){compose.onNodeWithText("Ajouter une opération").performClick();compose.onNodeWithText("Montant").performTextInput("42,50");compose.onNodeWithText("Catégorie").performClick();compose.onNodeWithText("Food").performClick();compose.onNodeWithText("Enregistrer").performClick();compose.waitUntil{transactions.saved.isNotEmpty()};val value=transactions.saved.single();assertEquals(TransactionType.EXPENSE,value.type);assertEquals(4_250,value.amount);assertEquals("cash",value.account);assertEquals("food",value.category)}
}
private data class SavedTransaction(val type:TransactionType,val amount:Long,val account:String,val destination:String?,val category:String?)
private class FakeTransactions:TransactionRepository{val values=MutableStateFlow<List<FinanceTransaction>>(emptyList());val saved=mutableListOf<SavedTransaction>();override fun observeTransactions():Flow<List<FinanceTransaction>> =values;override suspend fun create(type:TransactionType,amountMinor:Long,accountId:String,destinationAccountId:String?,categoryId:String?,description:String,subcategoryId:String?,currencyCode:String){saved+=SavedTransaction(type,amountMinor,accountId,destinationAccountId,categoryId);values.value=listOf(FinanceTransaction("id",type,amountMinor,currencyCode,accountId,"Cash",categoryId=categoryId,categoryCustomName="Food",occurredAt=1))};override suspend fun delete(id:String)=Unit}
private class FakeTransactionAccounts:AccountRepository{override fun observeAccounts()=flowOf(listOf(Account("cash","Cash",AccountType.CASH,"DZD",0,0),Account("bank","Bank",AccountType.BANK,"DZD",0,0)));override suspend fun create(name:String,type:AccountType,initialBalanceMinor:Long,currencyCode:String)=Unit;override suspend fun setArchived(id:String,archived:Boolean)=Unit}
private class FakeTransactionCategories:CategoryRepository{override fun observeCategories(kind:CategoryKind?)=flowOf(listOf(Category("food",CategoryKind.EXPENSE,customName="Food"),Category("salary",CategoryKind.INCOME,customName="Salary")));override fun observeSubcategories()=flowOf(emptyList<Subcategory>());override suspend fun create(name:String,kind:CategoryKind,iconKey:String,colorKey:String)=Unit;override suspend fun update(id:String,name:String,iconKey:String,colorKey:String)=Unit;override suspend fun createSubcategory(categoryId:String,name:String)=Unit;override suspend fun updateSubcategory(id:String,name:String)=Unit;override suspend fun setArchived(id:String,archived:Boolean,replacementId:String?)=Unit;override suspend fun setSubcategoryArchived(id:String,archived:Boolean,replacementId:String?)=Unit;override suspend fun delete(id:String,replacementId:String?)=Unit}
