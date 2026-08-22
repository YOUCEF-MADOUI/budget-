package com.budgetplusplus.core.designsystem

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import com.budgetplusplus.core.designsystem.financial.MoneyText
import com.budgetplusplus.core.designsystem.theme.BudgetPlusPlusTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35],qualifiers="fr")
class AccessibilitySemanticsTest {
 @get:Rule val compose=createComposeRule()
 @Test fun `hidden financial amount exposes localized accessible description`(){compose.setContent{BudgetPlusPlusTheme{MoneyText(100_000,"DZD",hidden=true)}};compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription,listOf("Montant masqué"))).assertExists()}
}
