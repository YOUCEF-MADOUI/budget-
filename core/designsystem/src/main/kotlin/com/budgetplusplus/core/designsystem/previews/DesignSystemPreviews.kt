package com.budgetplusplus.core.designsystem.previews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.budgetplusplus.core.designsystem.R
import com.budgetplusplus.core.designsystem.components.BudgetEmptyState
import com.budgetplusplus.core.designsystem.components.BudgetErrorState
import com.budgetplusplus.core.designsystem.financial.AmountTone
import com.budgetplusplus.core.designsystem.financial.BalanceCard
import com.budgetplusplus.core.designsystem.financial.FinancialBudgetCard
import com.budgetplusplus.core.designsystem.financial.MoneyText
import com.budgetplusplus.core.designsystem.financial.TransactionRow
import com.budgetplusplus.core.designsystem.financial.TransactionVisualType
import com.budgetplusplus.core.designsystem.icons.BudgetIcons
import com.budgetplusplus.core.designsystem.theme.BudgetPlusPlusTheme
import com.budgetplusplus.core.designsystem.tokens.BudgetSpacing

@Preview(name = "Light", showBackground = true, widthDp = 380)
@Preview(name = "Dark", showBackground = true, widthDp = 380, uiMode = Configuration.UI_MODE_NIGHT_YES)
private annotation class BudgetThemePreviews

@Preview(name = "Arabic RTL", showBackground = true, widthDp = 380, locale = "ar")
@Preview(name = "Large text", showBackground = true, widthDp = 380, fontScale = 1.6f)
private annotation class BudgetAccessibilityPreviews

@BudgetThemePreviews
@Composable
private fun BalancePositivePreview() {
    BudgetPlusPlusTheme {
        BalanceCard(
            title = stringResource(R.string.ds_demo_balance_title),
            balanceMinor = 124_560_000L,
            currencyCode = "DZD",
            variation = stringResource(R.string.ds_demo_variation),
            onToggleVisibility = {},
            modifier = Modifier.padding(BudgetSpacing.Md),
        )
    }
}

@BudgetAccessibilityPreviews
@Composable
private fun BalanceNegativeAccessiblePreview() {
    BudgetPlusPlusTheme {
        BalanceCard(
            title = stringResource(R.string.ds_demo_balance_title),
            balanceMinor = -12_450_000L,
            currencyCode = "DZD",
            onToggleVisibility = {},
            modifier = Modifier.padding(BudgetSpacing.Md),
        )
    }
}

@BudgetThemePreviews
@Composable
private fun BudgetStatesPreview() {
    BudgetPlusPlusTheme {
        Column(modifier = Modifier.padding(BudgetSpacing.Md)) {
            FinancialBudgetCard(
                name = stringResource(R.string.ds_budget_normal),
                plannedLabel = stringResource(R.string.ds_demo_planned),
                spentLabel = stringResource(R.string.ds_demo_spent),
                remainingLabel = stringResource(R.string.ds_demo_remaining),
                plannedMinor = 5_000_000,
                spentMinor = 2_500_000,
                currencyCode = "DZD",
            )
            FinancialBudgetCard(
                name = stringResource(R.string.ds_budget_warning),
                plannedLabel = stringResource(R.string.ds_demo_planned),
                spentLabel = stringResource(R.string.ds_demo_spent),
                remainingLabel = stringResource(R.string.ds_demo_remaining),
                plannedMinor = 5_000_000,
                spentMinor = 4_600_000,
                currencyCode = "DZD",
            )
            FinancialBudgetCard(
                name = stringResource(R.string.ds_budget_exceeded),
                plannedLabel = stringResource(R.string.ds_demo_planned),
                spentLabel = stringResource(R.string.ds_demo_spent),
                remainingLabel = stringResource(R.string.ds_demo_remaining),
                plannedMinor = 5_000_000,
                spentMinor = 5_500_000,
                currencyCode = "DZD",
            )
        }
    }
}

@BudgetAccessibilityPreviews
@Composable
private fun TransactionRtlAndLargeTextPreview() {
    BudgetPlusPlusTheme {
        TransactionRow(
            category = stringResource(R.string.ds_demo_category),
            description = stringResource(R.string.ds_demo_description),
            date = stringResource(R.string.ds_demo_date),
            account = stringResource(R.string.ds_demo_account_value),
            amountMinor = -580_000,
            currencyCode = "DZD",
            type = TransactionVisualType.Expense,
            categoryIcon = BudgetIcons.Category,
            recurring = true,
            modifier = Modifier.padding(BudgetSpacing.Md),
        )
    }
}

@BudgetThemePreviews
@Composable
private fun FeedbackStatesPreview() {
    BudgetPlusPlusTheme {
        Column {
            BudgetEmptyState(
                title = stringResource(R.string.ds_demo_empty_title),
                message = stringResource(R.string.ds_demo_empty_message),
            )
            BudgetErrorState(
                title = stringResource(R.string.ds_demo_error_title),
                message = stringResource(R.string.ds_demo_error_message),
                onRetry = {},
            )
        }
    }
}

@BudgetThemePreviews
@Composable
private fun AmountVariantsPreview() {
    BudgetPlusPlusTheme {
        Column(modifier = Modifier.padding(BudgetSpacing.Md)) {
            MoneyText(450_000_00L, "DZD", tone = AmountTone.Income)
            MoneyText(-5_800_00L, "DZD", tone = AmountTone.Expense)
            MoneyText(125_000L, "EUR", tone = AmountTone.Neutral)
            MoneyText(125_000L, "USD", tone = AmountTone.Transfer)
            MoneyText(0L, "DZD", hidden = true)
        }
    }
}
