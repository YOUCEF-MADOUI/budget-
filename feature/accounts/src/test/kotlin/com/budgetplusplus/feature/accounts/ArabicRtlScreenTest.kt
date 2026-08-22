package com.budgetplusplus.feature.accounts

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.budgetplusplus.core.designsystem.theme.BudgetPlusPlusTheme
import com.budgetplusplus.core.model.*
import com.budgetplusplus.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35],qualifiers="ar")
class ArabicRtlScreenTest {
 @get:Rule val compose=createComposeRule()
 @Test fun `arabic resources activate RTL layout`(){val viewModel=AccountsViewModel(EmptyAccounts());compose.setContent{BudgetPlusPlusTheme{val direction=LocalLayoutDirection.current;Box(Modifier.testTag("rtl-root").semantics{stateDescription=direction.name}){AccountsScreen(null,viewModel)}}};compose.onNodeWithText("لا توجد حسابات").assertExists();compose.onNodeWithTag("rtl-root").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription,"Rtl"))}
}
private class EmptyAccounts:AccountRepository{override fun observeAccounts():Flow<List<Account>> = flowOf(emptyList());override suspend fun create(name:String,type:AccountType,initialBalanceMinor:Long,currencyCode:String)=Unit;override suspend fun setArchived(id:String,archived:Boolean)=Unit}
