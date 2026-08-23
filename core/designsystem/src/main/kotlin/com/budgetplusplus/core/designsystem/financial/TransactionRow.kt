package com.budgetplusplus.core.designsystem.financial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.budgetplusplus.core.designsystem.R
import com.budgetplusplus.core.designsystem.components.BudgetCategoryIcon
import com.budgetplusplus.core.designsystem.components.BudgetListRow
import com.budgetplusplus.core.designsystem.theme.financialColors
import com.budgetplusplus.core.designsystem.tokens.BudgetSizes
import com.budgetplusplus.core.designsystem.tokens.BudgetSpacing

enum class TransactionVisualType {
    Income,
    Expense,
    Transfer,
}

@Composable
fun TransactionRow(
    category: String,
    description: String,
    date: String,
    account: String,
    amountMinor: Long,
    currencyCode: String,
    type: TransactionVisualType,
    categoryIcon: ImageVector,
    modifier: Modifier = Modifier,
    recurring: Boolean = false,
    categoryImage: ImageBitmap? = null,
    onClick: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.financialColors
    val tone = when (type) {
        TransactionVisualType.Income -> AmountTone.Income
        TransactionVisualType.Expense -> AmountTone.Expense
        TransactionVisualType.Transfer -> AmountTone.Transfer
    }
    val containerColor = when (type) {
        TransactionVisualType.Income -> colors.incomeContainer
        TransactionVisualType.Expense -> colors.expenseContainer
        TransactionVisualType.Transfer -> colors.transferContainer
    }
    val contentColor = when (type) {
        TransactionVisualType.Income -> colors.onIncomeContainer
        TransactionVisualType.Expense -> colors.onExpenseContainer
        TransactionVisualType.Transfer -> colors.onTransferContainer
    }
    BudgetListRow(
        headline = category,
        supportingText = listOf(description, date, account).filter { it.isNotBlank() }.joinToString(" · "),
        onClick = onClick,
        modifier = modifier,
        leadingContent = {
            if (categoryImage != null) {
                Image(
                    bitmap = categoryImage,
                    contentDescription = category,
                    modifier = Modifier.size(BudgetSizes.CategoryIcon).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else BudgetCategoryIcon(
                icon = categoryIcon,
                contentDescription = null,
                containerColor = containerColor,
                contentColor = contentColor,
            )
        },
        trailingContent = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Xxs),
            ) {
                MoneyText(
                    minorUnits = amountMinor,
                    currencyCode = currencyCode,
                    tone = tone,
                )
                if (recurring) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = stringResource(R.string.ds_recurring_transaction),
                            modifier = Modifier.size(BudgetSizes.IconSmall),
                        )
                    }
                }
            }
        },
    )
}
