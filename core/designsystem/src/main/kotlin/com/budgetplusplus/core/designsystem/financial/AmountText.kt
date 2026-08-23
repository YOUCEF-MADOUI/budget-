package com.budgetplusplus.core.designsystem.financial

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import com.budgetplusplus.core.designsystem.R
import com.budgetplusplus.core.designsystem.theme.CompactAmountStyle
import com.budgetplusplus.core.designsystem.theme.financialColors
import com.budgetplusplus.core.designsystem.util.MoneyFormatter
import java.util.Locale

enum class AmountTone {
    Income,
    Expense,
    Transfer,
    Neutral,
}

@Composable
fun MoneyText(
    minorUnits: Long,
    currencyCode: String,
    modifier: Modifier = Modifier,
    tone: AmountTone = AmountTone.Neutral,
    hidden: Boolean = false,
    style: TextStyle = CompactAmountStyle,
    locale: Locale = Locale.getDefault(),
) {
    val formatted = if (hidden) {
        "••••••"
    } else {
        MoneyFormatter.format(
            minorUnits = minorUnits,
            currencyCode = currencyCode,
            locale = locale,
            showPositiveSign = tone == AmountTone.Income,
        )
    }
    val accessibleText = if (hidden) {
        stringResource(R.string.ds_amount_hidden)
    } else {
        when (tone) {
            AmountTone.Income -> stringResource(R.string.ds_income_amount, formatted)
            AmountTone.Expense -> stringResource(R.string.ds_expense_amount, formatted)
            AmountTone.Transfer -> stringResource(R.string.ds_transfer_amount, formatted)
            AmountTone.Neutral -> stringResource(R.string.ds_neutral_amount, formatted)
        }
    }
    Text(
        text = formatted,
        modifier = modifier.clearAndSetSemantics { contentDescription = accessibleText },
        color = amountColor(tone),
        style = style,
        softWrap = true,
    )
}

@Composable
private fun amountColor(tone: AmountTone): Color = when (tone) {
    AmountTone.Income -> MaterialTheme.financialColors.income
    AmountTone.Expense -> MaterialTheme.financialColors.expense
    AmountTone.Transfer -> MaterialTheme.financialColors.transfer
    AmountTone.Neutral -> MaterialTheme.colorScheme.onSurface
}
