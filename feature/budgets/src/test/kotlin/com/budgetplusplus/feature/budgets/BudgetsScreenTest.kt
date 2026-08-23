package com.budgetplusplus.feature.budgets

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.budgetplusplus.core.designsystem.financial.FinancialBudgetCard
import com.budgetplusplus.core.designsystem.theme.BudgetPlusPlusTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35],qualifiers="fr")
class BudgetsScreenTest {
 @get:Rule val compose=createComposeRule()
 @Test fun `budget progress card exposes complete textual alternative`(){compose.setContent{BudgetPlusPlusTheme{FinancialBudgetCard(name="Courses",plannedLabel="Prévu",spentLabel="Dépensé",remainingLabel="Restant",plannedMinor=500_000,spentMinor=375_000,currencyCode="DZD",warningThresholdPercent=75)}};compose.onNodeWithText("Courses").assertExists();compose.onNodeWithText("Prévu").assertExists();compose.onNodeWithText("Dépensé").assertExists();compose.onNodeWithText("Restant").assertExists()}
}
