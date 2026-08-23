package com.budgetplusplus.core.designsystem.financial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.budgetplusplus.core.designsystem.R
import com.budgetplusplus.core.designsystem.components.BudgetBadge
import com.budgetplusplus.core.designsystem.components.BudgetCard
import com.budgetplusplus.core.designsystem.theme.BalanceDisplayStyle
import com.budgetplusplus.core.designsystem.tokens.BudgetSpacing

@Composable
fun BalanceCard(
    title: String,
    balanceMinor: Long,
    currencyCode: String,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
    variation: String? = null,
    onToggleVisibility: (() -> Unit)? = null,
) {
    val tone = when {
        balanceMinor > 0 -> AmountTone.Income
        balanceMinor < 0 -> AmountTone.Expense
        else -> AmountTone.Neutral
    }
    val stateLabel = stringResource(
        when (tone) {
            AmountTone.Income -> R.string.ds_balance_positive
            AmountTone.Expense -> R.string.ds_balance_negative
            else -> R.string.ds_balance_neutral
        },
    )
    BudgetCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (onToggleVisibility != null) {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            imageVector = if (hidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = stringResource(
                                if (hidden) R.string.ds_balance_hidden else R.string.ds_balance_visible,
                            ),
                        )
                    }
                }
            }
            MoneyText(
                minorUnits = balanceMinor,
                currencyCode = currencyCode,
                hidden = hidden,
                tone = tone,
                style = BalanceDisplayStyle,
            )
            Column(verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Xs)) {
                BudgetBadge(stateLabel)
                variation?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
