package com.budgetplusplus.feature.accounts

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.budgetplusplus.core.designsystem.theme.BudgetPlusPlusTheme
import com.budgetplusplus.core.model.*
import com.budgetplusplus.domain.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35],qualifiers="fr")
class AccountsScreenTest {
 @get:Rule val compose=createComposeRule();private val repository=FakeAccounts();private lateinit var viewModel:AccountsViewModel
 @Before fun content(){viewModel=AccountsViewModel(repository);compose.setContent{BudgetPlusPlusTheme{AccountsScreen(onBack=null,viewModel=viewModel)}}}
 @Test fun `empty state is localized and account can be created`(){compose.onNodeWithText("Aucun compte").assertExists();compose.onNodeWithText("Ajouter un compte").performClick();compose.onNodeWithText("Nom du compte").performTextInput("Espèces");compose.onNodeWithText("Solde initial").performTextInput("1000,50");compose.onNodeWithText("Enregistrer").performClick();compose.waitUntil{repository.values.value.size==1};assertEquals(100_050,repository.values.value.single().initialBalanceMinor);compose.onNodeWithText("Espèces").assertExists()}
}
private class FakeAccounts:AccountRepository{val values=MutableStateFlow<List<Account>>(emptyList());override fun observeAccounts():Flow<List<Account>> =values;override suspend fun create(name:String,type:AccountType,initialBalanceMinor:Long,currencyCode:String){values.value=listOf(Account("id",name,type,currencyCode,initialBalanceMinor,initialBalanceMinor))};override suspend fun setArchived(id:String,archived:Boolean){}}
