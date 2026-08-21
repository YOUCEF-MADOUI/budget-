package com.budgetplusplus.core.designsystem.financial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetplusplus.core.designsystem.components.BudgetCard
import com.budgetplusplus.core.designsystem.tokens.BudgetSpacing

@Composable
fun IncomeExpenseCard(
    period: String,
    incomeLabel: String,
    expenseLabel: String,
    differenceLabel: String,
    incomeMinor: Long,
    expenseMinor: Long,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    BudgetCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Md)) {
            Text(period, style = MaterialTheme.typography.titleMedium)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Sm),
            ) {
                FinancialMetric(
                    label = incomeLabel,
                    amountMinor = incomeMinor,
                    currencyCode = currencyCode,
                    tone = AmountTone.Income,
                )
                FinancialMetric(
                    label = expenseLabel,
                    amountMinor = -expenseMinor.coerceAtLeast(0L),
                    currencyCode = currencyCode,
                    tone = AmountTone.Expense,
                )
            }
            FinancialMetric(
                label = differenceLabel,
                amountMinor = incomeMinor - expenseMinor,
                currencyCode = currencyCode,
                tone = if (incomeMinor >= expenseMinor) AmountTone.Income else AmountTone.Expense,
            )
        }
    }
}

@Composable
private fun FinancialMetric(
    label: String,
    amountMinor: Long,
    currencyCode: String,
    tone: AmountTone,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Xxs)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        MoneyText(amountMinor, currencyCode, tone = tone)
    }
}
