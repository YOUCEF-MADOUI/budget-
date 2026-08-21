package com.budgetplusplus.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.budgetplusplus.core.designsystem.tokens.BudgetElevation
import com.budgetplusplus.core.designsystem.tokens.BudgetSizes
import com.budgetplusplus.core.designsystem.tokens.BudgetSpacing

@Composable
fun BudgetCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetElevation.Low),
        border = BorderStroke(BudgetSizes.CardBorder, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(Modifier.padding(BudgetSpacing.Md)) { content() }
    }
}

@Composable
fun BudgetClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetElevation.Low),
        border = BorderStroke(BudgetSizes.CardBorder, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(Modifier.padding(BudgetSpacing.Md)) { content() }
    }
}

@Composable
fun BudgetSummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    BudgetCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Xs)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, color = accent)
            supportingText?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun BudgetListRow(
    headline: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = BudgetSpacing.Md, vertical = BudgetSpacing.Sm),
        horizontalArrangement = Arrangement.spacedBy(BudgetSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent?.invoke()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Xxs),
        ) {
            Text(headline, style = MaterialTheme.typography.bodyLarge)
            supportingText?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailingContent?.invoke(this)
    }
}

@Composable
fun BudgetCategoryIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Surface(
        modifier = modifier.size(BudgetSizes.CategoryIcon),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(BudgetSizes.IconMedium),
            )
        }
    }
}

@Composable
fun BudgetDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
fun BudgetBadge(text: String, modifier: Modifier = Modifier) {
    Badge(modifier = modifier.widthIn(min = 24.dp)) { Text(text) }
}

@Composable
fun BudgetFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(text) },
        modifier = modifier,
    )
}

@Composable
fun BudgetAssistChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AssistChip(onClick = onClick, label = { Text(text) }, modifier = modifier)
}
