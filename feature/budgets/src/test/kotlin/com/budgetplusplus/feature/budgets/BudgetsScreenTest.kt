package com.budgetplusplus.feature.budgets

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
class BudgetsScreenTest {
 @get:Rule val compose=createComposeRule();private val budgets=FakeBudgets()
 @Before fun content(){compose.setContent{BudgetPlusPlusTheme{BudgetsScreen(null,BudgetsViewModel(budgets,EmptyCategories()))}}}
 @Test fun `global monthly budget can be created from empty state`(){compose.onNodeWithText("Aucun budget actif").assertExists();compose.onNodeWithText("Créer un budget").performClick();compose.onNodeWithText("Nom").performTextInput("Courses");compose.onNodeWithText("Montant prévu").performTextInput("5000");compose.onNodeWithText("Enregistrer").performClick();compose.waitUntil{budgets.values.value.size==1};assertEquals(500_000,budgets.saved?.amountMinor);assertEquals(BudgetScope.GLOBAL,budgets.saved?.scope)}
}
private class FakeBudgets:BudgetRepository{val values=MutableStateFlow<List<BudgetProgress>>(emptyList());var saved:BudgetInput?=null;override fun observeBudgets():Flow<List<BudgetProgress>> =values;override suspend fun save(input:BudgetInput){saved=input;values.value=listOf(BudgetProgress("id",input.name,input.scope,amountMinor=input.amountMinor,spentMinor=0,currencyCode="DZD",periodType=input.periodType,startDate=input.startDate,endDate=input.endDate,warningThresholdPercent=input.warningThresholdPercent,isArchived=false))};override suspend fun setArchived(id:String,archived:Boolean)=Unit}
private class EmptyCategories:CategoryRepository{override fun observeCategories(kind:CategoryKind?)=flowOf(emptyList<Category>());override fun observeSubcategories()=flowOf(emptyList<Subcategory>());override suspend fun create(name:String,kind:CategoryKind,iconKey:String,colorKey:String)=Unit;override suspend fun update(id:String,name:String,iconKey:String,colorKey:String)=Unit;override suspend fun createSubcategory(categoryId:String,name:String)=Unit;override suspend fun updateSubcategory(id:String,name:String)=Unit;override suspend fun setArchived(id:String,archived:Boolean,replacementId:String?)=Unit;override suspend fun setSubcategoryArchived(id:String,archived:Boolean,replacementId:String?)=Unit;override suspend fun delete(id:String,replacementId:String?)=Unit}
