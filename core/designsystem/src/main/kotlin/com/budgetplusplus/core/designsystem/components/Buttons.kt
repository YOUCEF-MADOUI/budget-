package com.budgetplusplus.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import com.budgetplusplus.core.designsystem.R
import com.budgetplusplus.core.designsystem.tokens.BudgetSizes
import com.budgetplusplus.core.designsystem.tokens.BudgetSpacing

@Composable
fun BudgetPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    val loadingDescription = stringResource(R.string.ds_loading)
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .height(BudgetSizes.ButtonHeight)
            .semantics { if (loading) stateDescription = loadingDescription },
    ) {
        ButtonContent(text, loading, leadingIcon)
    }
}

@Composable
fun BudgetSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(BudgetSizes.ButtonHeight),
    ) {
        ButtonContent(text = text, loading = false, leadingIcon = leadingIcon)
    }
}

@Composable
fun BudgetTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(BudgetSizes.MinimumTouchTarget),
    ) {
        Text(text)
    }
}

@Composable
fun BudgetFullWidthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    BudgetPrimaryButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        loading = loading,
        modifier = modifier
            .fillMaxWidth()
            .height(BudgetSizes.LargeButtonHeight),
    )
}

@Composable
fun BudgetAddFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
) {
    val label = stringResource(R.string.ds_add)
    if (expanded) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text(label) },
        )
    } else {
        androidx.compose.material3.FloatingActionButton(
            onClick = onClick,
            modifier = modifier.size(BudgetSizes.Fab),
        ) {
            Icon(Icons.Default.Add, contentDescription = label)
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    leadingIcon: ImageVector?,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(BudgetSizes.IconSmall),
                    strokeWidth = BudgetSpacing.Xxs / 2,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.width(BudgetSpacing.Xs))
            }
            leadingIcon != null -> {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(BudgetSizes.IconSmall))
                Spacer(Modifier.width(BudgetSpacing.Xs))
            }
        }
        Text(text)
    }
}
