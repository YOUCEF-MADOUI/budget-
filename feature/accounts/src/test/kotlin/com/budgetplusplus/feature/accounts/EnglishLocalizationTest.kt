package com.budgetplusplus.feature.accounts

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createComposeRule
import com.budgetplusplus.core.designsystem.theme.BudgetPlusPlusTheme
import com.budgetplusplus.core.model.*
import com.budgetplusplus.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35],qualifiers="en")
class EnglishLocalizationTest {
 @get:Rule val compose=createComposeRule()
 @Test fun `account empty state is translated to English`(){val viewModel=AccountsViewModel(EnglishEmptyAccounts());compose.setContent{BudgetPlusPlusTheme{AccountsScreen(null,viewModel)}};compose.onNodeWithText("No accounts").assertExists()}
}
private class EnglishEmptyAccounts:AccountRepository{override fun observeAccounts():Flow<List<Account>> = flowOf(emptyList());override suspend fun create(name:String,type:AccountType,initialBalanceMinor:Long,currencyCode:String)=Unit;override suspend fun setArchived(id:String,archived:Boolean)=Unit}
