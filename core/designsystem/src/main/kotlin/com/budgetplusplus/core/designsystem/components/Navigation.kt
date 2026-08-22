package com.budgetplusplus.core.designsystem.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.budgetplusplus.core.designsystem.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        title = { Text(title, modifier = Modifier.semantics { heading() }) },
        modifier = modifier,
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.ds_back),
                    )
                }
            }
        },
        actions = {
            if (onMoreClick != null) {
                IconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.ds_more_options),
                    )
                }
            }
        },
    )
}

@Immutable
data class BudgetNavigationItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun BudgetBottomNavigation(
    items: List<BudgetNavigationItem>,
    selectedItemId: String,
    onItemSelected: (BudgetNavigationItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.id == selectedItemId,
                onClick = { onItemSelected(item) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
            )
        }
    }
}
