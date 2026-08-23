package com.budgetplusplus.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.budgetplusplus.core.designsystem.R
import com.budgetplusplus.core.designsystem.tokens.BudgetSizes
import com.budgetplusplus.core.designsystem.tokens.BudgetSpacing

@Composable
fun BudgetLoadingIndicator(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.ds_loading)
    CircularProgressIndicator(
        modifier = modifier
            .size(BudgetSizes.IconLarge)
            .semantics { contentDescription = description },
    )
}

@Composable
fun BudgetConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = stringResource(R.string.ds_confirm),
    dismissText: String = stringResource(R.string.ds_cancel),
    destructive: Boolean = false,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissText) } },
    )
}

@Composable
fun BudgetSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier)
}

@Composable
fun BudgetEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    icon: ImageVector = Icons.Default.Info,
) {
    StateContent(
        title = title,
        message = message,
        modifier = modifier,
        icon = icon,
        actionText = actionText,
        onAction = onAction,
    )
}

@Composable
fun BudgetErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StateContent(
        title = title,
        message = message,
        modifier = modifier,
        icon = Icons.Default.Warning,
        iconTint = MaterialTheme.colorScheme.error,
        actionText = stringResource(R.string.ds_retry),
        onAction = onRetry,
    )
}

@Composable
fun BudgetOfflineState(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    StateContent(
        title = stringResource(R.string.ds_offline_title),
        message = stringResource(R.string.ds_offline_message),
        modifier = modifier,
        icon = Icons.Default.Warning,
        iconTint = MaterialTheme.colorScheme.tertiary,
        actionText = if (onRetry != null) stringResource(R.string.ds_retry) else null,
        onAction = onRetry,
    )
}

@Composable
fun BudgetSkeleton(
    modifier: Modifier = Modifier,
    lines: Int = 3,
) {
    val description = stringResource(R.string.ds_loading)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Xs),
    ) {
        repeat(lines.coerceAtLeast(1)) { index ->
            Spacer(
                Modifier
                    .fillMaxWidth(if (index == lines - 1) 0.65f else 1f)
                    .height(BudgetSizes.SkeletonLine)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.small,
                    ),
            )
        }
    }
}

@Composable
private fun StateContent(
    title: String,
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(BudgetSpacing.Lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Sm),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(BudgetSizes.IconLarge),
        )
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionText != null && onAction != null) {
            BudgetSecondaryButton(
                text = actionText,
                onClick = onAction,
                leadingIcon = Icons.Default.Refresh,
            )
        }
    }
}
