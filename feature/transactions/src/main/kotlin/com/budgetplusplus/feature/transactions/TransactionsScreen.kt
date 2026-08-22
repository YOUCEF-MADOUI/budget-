package com.budgetplusplus.feature.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.budgetplusplus.core.designsystem.components.*
import com.budgetplusplus.core.designsystem.financial.TransactionRow
import com.budgetplusplus.core.designsystem.financial.TransactionVisualType
import com.budgetplusplus.core.designsystem.icons.BudgetIcons
import com.budgetplusplus.core.model.*
import com.budgetplusplus.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class TransactionsViewModel @Inject constructor(accountsRepository: AccountRepository, categoriesRepository: CategoryRepository, private val repository: TransactionRepository) : ViewModel() {
    val accounts = accountsRepository.observeAccounts().map { it.filterNot(Account::isArchived) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val subcategories = categoriesRepository.observeSubcategories().map { values -> values.filterNot(Subcategory::isArchived) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories = categoriesRepository.observeCategories().map { it.filterNot(Category::isArchived) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val transactions = repository.observeTransactions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _hasError = MutableStateFlow(false)
    val hasError = _hasError.asStateFlow()
    fun add(type: TransactionType, amount: Long, account: String, destination: String?, category: String?, subcategory: String?, description: String, onSuccess: () -> Unit) = viewModelScope.launch {
        runCatching { repository.create(type, amount, account, destination, category, description, subcategory) }.onSuccess { _hasError.value = false; onSuccess() }.onFailure { _hasError.value = true }
    }
    fun delete(id: String) = viewModelScope.launch { runCatching { repository.delete(id) }.onSuccess { _hasError.value = false }.onFailure { _hasError.value = true } }
    fun clearError() { _hasError.value = false }
}

@Composable
fun TransactionsScreen(onBack: (() -> Unit)?, onAdvancedSearch: () -> Unit, onCreate: () -> Unit, onEdit: (String) -> Unit, viewModel: TransactionsViewModel = hiltViewModel()) {
    val rows by viewModel.transactions.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val hasError by viewModel.hasError.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<FinanceTransaction?>(null) }
    var filter by remember { mutableStateOf<TransactionType?>(null) }
    var pendingDelete by remember { mutableStateOf<FinanceTransaction?>(null) }
    val visibleRows = rows.filter { filter == null || it.type == filter }
    val snackbar = remember { SnackbarHostState() }
    val error = stringResource(R.string.transactions_error)
    LaunchedEffect(hasError) { if (hasError) { snackbar.showSnackbar(error); viewModel.clearError() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { BudgetTopAppBar(stringResource(R.string.transactions_title), onBackClick = onBack) },
        floatingActionButton = { FloatingActionButton(onClick = { if (accounts.isNotEmpty()) onCreate() }) { Text(stringResource(R.string.transactions_add_symbol)) } },
    ) { padding -> Column(Modifier.padding(padding).fillMaxSize()) {
        Button(onClick = onAdvancedSearch, modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth()) { Text(stringResource(R.string.transactions_advanced_search)) }
        if (rows.isNotEmpty()) {
            LazyRow(Modifier.padding(horizontal = 12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item { FilterChip(filter == null, { filter = null }, { Text(stringResource(R.string.transactions_all)) }) }
                items(TransactionType.entries) { type -> FilterChip(filter == type, { filter = type }, { Text(typeLabel(type)) }) }
            }
            Text(stringResource(R.string.transactions_count, visibleRows.size), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
        if (visibleRows.isEmpty()) BudgetEmptyState(
            title = stringResource(if (filter == null) R.string.transactions_empty else R.string.transactions_no_result),
            message = stringResource(if (accounts.isEmpty()) R.string.transactions_need_account else if (filter == null) R.string.transactions_empty_message else R.string.transactions_no_result_message),
            actionText = if (accounts.isEmpty()) null else stringResource(R.string.transactions_add_action),
            onAction = if (accounts.isEmpty()) null else onCreate,
            modifier = Modifier.fillMaxSize(),
        ) else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(visibleRows, key = { it.id }) { transaction ->
                val destinationName = transaction.destinationAccountName
                val accountLabel = destinationName?.let { stringResource(R.string.transactions_transfer_accounts, transaction.accountName, it) } ?: transaction.accountName
                TransactionRow(
                    category = transaction.categoryCustomName ?: transaction.categoryNameKey?.let { transactionCategory(it) } ?: typeLabel(transaction.type),
                    description = transaction.description,
                    date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(transaction.occurredAt)),
                    account = accountLabel,
                    amountMinor = transaction.amountMinor,
                    currencyCode = transaction.currencyCode,
                    type = visualType(transaction.type),
                    categoryIcon = BudgetIcons.Category,
                    onClick = { selected = transaction },
                )
            }
        }
    } }
    selected?.let { transaction -> AlertDialog(onDismissRequest = { selected = null }, title = { Text(stringResource(R.string.transactions_action_title)) }, text = { Text(stringResource(R.string.transactions_action_message)) }, confirmButton = { TextButton({ selected = null; onEdit(transaction.id) }) { Text(stringResource(R.string.transactions_edit)) } }, dismissButton = { Row { TextButton({ selected = null }) { Text(stringResource(R.string.transactions_cancel_action)) }; TextButton({ selected = null; pendingDelete = transaction }) { Text(stringResource(R.string.transactions_delete)) } } }) }
    pendingDelete?.let { transaction -> BudgetConfirmationDialog(
        title = stringResource(R.string.transactions_delete_title),
        message = stringResource(R.string.transactions_delete_message),
        confirmText = stringResource(R.string.transactions_delete),
        destructive = true,
        onConfirm = { viewModel.delete(transaction.id); pendingDelete = null },
        onDismiss = { pendingDelete = null },
    ) }
}

@Composable private fun transactionCategory(key: String) = stringResource(when(key) { "category_food" -> R.string.transaction_food; "category_transport" -> R.string.transaction_transport; "category_housing" -> R.string.transaction_housing; "category_health" -> R.string.transaction_health; "category_leisure" -> R.string.transaction_leisure; "category_utilities" -> R.string.transaction_utilities; "category_education" -> R.string.transaction_education; "category_family" -> R.string.transaction_family; "category_clothing" -> R.string.transaction_clothing; "category_taxes" -> R.string.transaction_taxes; "category_salary" -> R.string.transaction_salary; "category_freelance" -> R.string.transaction_freelance; "category_pension" -> R.string.transaction_pension; "category_benefits" -> R.string.transaction_benefits; "category_gift" -> R.string.transaction_gift; else -> R.string.transaction_other })
@Composable private fun typeLabel(type: TransactionType) = stringResource(when(type) { TransactionType.EXPENSE -> R.string.transaction_expense; TransactionType.INCOME -> R.string.transaction_income; TransactionType.TRANSFER -> R.string.transaction_transfer })
private fun visualType(type: TransactionType) = when(type) { TransactionType.EXPENSE -> TransactionVisualType.Expense; TransactionType.INCOME -> TransactionVisualType.Income; TransactionType.TRANSFER -> TransactionVisualType.Transfer }
internal fun parseMinor(value: String): Long? { val p=value.trim().replace(',','.').split('.'); if(p.size>2||p.any{part->part.any{!it.isDigit()}})return null; val major=p[0].ifEmpty{"0"}.toLongOrNull()?:return null; val minor=p.getOrNull(1).orEmpty(); if(minor.length>2)return null; return runCatching{Math.addExact(Math.multiplyExact(major,100),minor.padEnd(2,'0').ifEmpty{"0"}.toLong())}.getOrNull() }
