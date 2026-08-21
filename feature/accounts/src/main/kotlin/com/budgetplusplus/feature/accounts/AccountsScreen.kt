package com.budgetplusplus.feature.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetplusplus.core.designsystem.components.BudgetAmountField
import com.budgetplusplus.core.designsystem.components.BudgetTextField
import com.budgetplusplus.core.designsystem.components.BudgetTopAppBar
import com.budgetplusplus.core.designsystem.components.BudgetEmptyState
import com.budgetplusplus.core.designsystem.financial.AccountVisualState
import com.budgetplusplus.core.designsystem.icons.BudgetIcons
import com.budgetplusplus.core.designsystem.financial.AccountCard
import com.budgetplusplus.core.model.AccountType

@Composable
fun AccountsScreen(onBack: () -> Unit, viewModel: AccountsViewModel = hiltViewModel()) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    Scaffold(topBar = { BudgetTopAppBar(stringResource(R.string.accounts_title), onBackClick = onBack) }, floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }) { Text(stringResource(R.string.accounts_add_symbol)) } }) { padding ->
        if (accounts.isEmpty()) BudgetEmptyState(title = stringResource(R.string.accounts_empty_title), message = stringResource(R.string.accounts_empty_message), modifier = Modifier.padding(padding))
        else LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(accounts, key = { it.id }) { account ->
                AccountCard(name = account.name, type = accountTypeLabel(account.type), balanceMinor = account.currentBalanceMinor, currencyCode = account.currencyCode, icon = BudgetIcons.Account, onClick = { viewModel.archive(account) }, state = if (account.isArchived) AccountVisualState.Archived else AccountVisualState.Active)
                if (account.isArchived) Text(stringResource(R.string.accounts_archived), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
    if (showAdd) AddAccountDialog(onDismiss = { showAdd = false }, onSave = { name, type, amount -> viewModel.add(name, type, amount); showAdd = false })
}

@Composable private fun AddAccountDialog(onDismiss: () -> Unit, onSave: (String, AccountType, Long) -> Unit) {
    var name by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; var type by remember { mutableStateOf(AccountType.CASH) }; var expanded by remember { mutableStateOf(false) }
    val parsed = parseAmountMinor(amount)
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.accounts_add_title)) }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BudgetTextField(name, { name = it }, stringResource(R.string.accounts_name))
        Box { OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(accountTypeLabel(type)) }; DropdownMenu(expanded, { expanded = false }) { AccountType.entries.forEach { item -> DropdownMenuItem({ Text(accountTypeLabel(item)) }, { type = item; expanded = false }) } } }
        BudgetAmountField(amount, { amount = it }, stringResource(R.string.accounts_initial_balance), stringResource(R.string.accounts_currency))
    } }, confirmButton = { TextButton(onClick = { onSave(name, type, parsed ?: 0) }, enabled = name.isNotBlank() && parsed != null) { Text(stringResource(R.string.accounts_save)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.accounts_cancel)) } })
}

@Composable private fun accountTypeLabel(type: AccountType) = stringResource(when(type) { AccountType.CASH -> R.string.account_type_cash; AccountType.BANK -> R.string.account_type_bank; AccountType.SAVINGS -> R.string.account_type_savings; AccountType.CARD -> R.string.account_type_card; AccountType.OTHER -> R.string.account_type_other })

internal fun parseAmountMinor(value: String): Long? {
    val normalized = value.trim().replace(',', '.')
    if (normalized.isEmpty()) return 0
    val parts = normalized.split('.')
    if (parts.size > 2 || parts.any { it.any { char -> !char.isDigit() } }) return null
    val major = parts[0].ifEmpty { "0" }.toLongOrNull() ?: return null
    val minorText = parts.getOrNull(1).orEmpty()
    if (minorText.length > 2) return null
    return runCatching { Math.addExact(Math.multiplyExact(major, 100), minorText.padEnd(2, '0').ifEmpty { "0" }.toLong()) }.getOrNull()
}
