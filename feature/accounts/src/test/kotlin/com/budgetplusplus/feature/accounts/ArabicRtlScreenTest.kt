package com.budgetplusplus.feature.accounts

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import com.budgetplusplus.core.designsystem.theme.BudgetPlusPlusTheme
import com.budgetplusplus.core.model.*
import com.budgetplusplus.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35],qualifiers="ar-rDZ-ldrtl")
class ArabicRtlScreenTest {
 @get:Rule val compose=createComposeRule()
 @Test fun `arabic resources activate RTL layout`(){val viewModel=AccountsViewModel(EmptyAccounts());var observed:LayoutDirection?=null;compose.setContent{BudgetPlusPlusTheme{observed=LocalLayoutDirection.current;AccountsScreen(null,viewModel)}};compose.waitForIdle();assertEquals(LayoutDirection.Rtl,observed);compose.onNodeWithText("لا توجد حسابات").assertExists()}
}
private class EmptyAccounts:AccountRepository{override fun observeAccounts():Flow<List<Account>> = flowOf(emptyList());override suspend fun create(name:String,type:AccountType,initialBalanceMinor:Long,currencyCode:String)=Unit;override suspend fun setArchived(id:String,archived:Boolean)=Unit}
