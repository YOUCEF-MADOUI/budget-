package com.budgetplusplus.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomePlaceholderScreen(
    onDesignSystemClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
    ) {
        Text(stringResource(R.string.technical_home_title), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(stringResource(R.string.technical_home_message), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onTransactionsClick, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.home_add_transaction)) }
        OutlinedButton(onClick = onAccountsClick, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.home_accounts)) }
        OutlinedButton(onClick = onCategoriesClick, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.home_categories)) }
        OutlinedButton(onClick = onTransactionsClick, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.home_transactions)) }
        OutlinedButton(onClick = onDesignSystemClick) { Text(stringResource(R.string.technical_open_design_system)) }
    }
}
