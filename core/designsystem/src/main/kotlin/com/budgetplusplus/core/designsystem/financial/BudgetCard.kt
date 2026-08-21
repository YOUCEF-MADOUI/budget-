package com.budgetplusplus.core.designsystem.financial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.budgetplusplus.core.designsystem.R
import com.budgetplusplus.core.designsystem.components.BudgetBadge
import com.budgetplusplus.core.designsystem.components.BudgetCard
import com.budgetplusplus.core.designsystem.tokens.BudgetSizes
import com.budgetplusplus.core.designsystem.tokens.BudgetSpacing
import com.budgetplusplus.core.designsystem.util.MoneyFormatter

@Composable
fun FinancialBudgetCard(
    name: String,
    plannedLabel: String,
    spentLabel: String,
    remainingLabel: String,
    plannedMinor: Long,
    spentMinor: Long,
    currencyCode: String,
    modifier: Modifier = Modifier,
    warningThresholdPercent: Int = 90,
) {
    val percentage = budgetUsedPercentage(spentMinor, plannedMinor)
    val state = budgetVisualState(spentMinor, plannedMinor, warningThresholdPercent)
    val remainingMinor = safeSubtract(plannedMinor, spentMinor)
    val remainingFormatted = MoneyFormatter.format(remainingMinor, currencyCode)
    val stateText = stringResource(
        when (state) {
            BudgetVisualState.Normal -> R.string.ds_budget_normal
            BudgetVisualState.NearLimit -> R.string.ds_budget_warning
            BudgetVisualState.Exceeded -> R.string.ds_budget_exceeded
        },
    )
    val progressDescription = stringResource(R.string.ds_budget_progress, percentage, remainingFormatted)
    val indicatorColor = budgetStateColor(state)

    BudgetCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                BudgetBadge("$percentage %")
            }
            LinearProgressIndicator(
                progress = { budgetProgress(spentMinor, plannedMinor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BudgetSizes.ProgressHeight)
                    .semantics { contentDescription = progressDescription },
                color = indicatorColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Xs),
            ) {
                BudgetMetric(plannedLabel, plannedMinor, currencyCode, AmountTone.Neutral)
                BudgetMetric(spentLabel, -spentMinor.coerceAtLeast(0), currencyCode, AmountTone.Expense)
                BudgetMetric(
                    remainingLabel,
                    remainingMinor,
                    currencyCode,
                    if (remainingMinor >= 0) AmountTone.Income else AmountTone.Expense,
                )
            }
            Text(stateText, style = MaterialTheme.typography.labelLarge, color = indicatorColor)
        }
    }
}

@Composable
private fun BudgetMetric(
    label: String,
    amountMinor: Long,
    currencyCode: String,
    tone: AmountTone,
) {
    Column(verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Xxs)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        MoneyText(amountMinor, currencyCode, tone = tone, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun budgetStateColor(state: BudgetVisualState): Color = when (state) {
    BudgetVisualState.Normal -> MaterialTheme.colorScheme.primary
    BudgetVisualState.NearLimit -> MaterialTheme.financialColors.budgetNearLimit
    BudgetVisualState.Exceeded -> MaterialTheme.financialColors.budgetExceeded
}

private fun safeSubtract(left: Long, right: Long): Long = try {
    Math.subtractExact(left, right)
} catch (_: ArithmeticException) {
    if (left >= 0L && right < 0L) Long.MAX_VALUE else Long.MIN_VALUE
}
