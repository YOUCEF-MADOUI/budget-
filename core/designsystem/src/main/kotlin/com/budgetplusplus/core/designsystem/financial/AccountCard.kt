package com.budgetplusplus.core.designsystem.financial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.budgetplusplus.core.designsystem.R
import com.budgetplusplus.core.designsystem.components.BudgetCategoryIcon
import com.budgetplusplus.core.designsystem.components.BudgetClickableCard
import com.budgetplusplus.core.designsystem.tokens.BudgetSpacing

enum class AccountVisualState {
    Active,
    Hidden,
    Archived,
}

@Composable
fun AccountCard(
    name: String,
    type: String,
    balanceMinor: Long,
    currencyCode: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: AccountVisualState = AccountVisualState.Active,
) {
    BudgetClickableCard(
        onClick = onClick,
        modifier = modifier.alpha(if (state == AccountVisualState.Archived) 0.72f else 1f),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BudgetSpacing.Sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BudgetCategoryIcon(icon = icon, contentDescription = null)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Xxs),
                ) {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    Text(type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (state != AccountVisualState.Active) {
                        Text(
                            text = stringResource(
                                if (state == AccountVisualState.Hidden) R.string.ds_account_hidden else R.string.ds_account_archived,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            MoneyText(
                minorUnits = balanceMinor,
                currencyCode = currencyCode,
                hidden = state == AccountVisualState.Hidden,
                tone = if (balanceMinor < 0) AmountTone.Expense else AmountTone.Neutral,
            )
        }
    }
}
