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
fun TransactionsScreen(onBack: (() -> Unit)?, viewModel: TransactionsViewModel = hiltViewModel()) {
    val rows by viewModel.transactions.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val subcategories by viewModel.subcategories.collectAsStateWithLifecycle()
    val hasError by viewModel.hasError.collectAsStateWithLifecycle()
    var add by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf<TransactionType?>(null) }
    var pendingDelete by remember { mutableStateOf<FinanceTransaction?>(null) }
    val visibleRows = rows.filter { filter == null || it.type == filter }
    val snackbar = remember { SnackbarHostState() }
    val error = stringResource(R.string.transactions_error)
    LaunchedEffect(hasError) { if (hasError) { snackbar.showSnackbar(error); viewModel.clearError() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { BudgetTopAppBar(stringResource(R.string.transactions_title), onBackClick = onBack) },
        floatingActionButton = { FloatingActionButton(onClick = { if (accounts.isNotEmpty()) add = true }) { Text(stringResource(R.string.transactions_add_symbol)) } },
    ) { padding -> Column(Modifier.padding(padding).fillMaxSize()) {
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
            onAction = if (accounts.isEmpty()) null else ({ add = true }),
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
                    onClick = { pendingDelete = transaction },
                )
            }
        }
    } }
    if (add) AddTransactionDialog(accounts, categories, subcategories, { add = false }) { type, amount, account, destination, category, subcategory, description -> viewModel.add(type, amount, account, destination, category, subcategory, description) { add = false } }
    pendingDelete?.let { transaction -> BudgetConfirmationDialog(
        title = stringResource(R.string.transactions_delete_title),
        message = stringResource(R.string.transactions_delete_message),
        confirmText = stringResource(R.string.transactions_delete),
        destructive = true,
        onConfirm = { viewModel.delete(transaction.id); pendingDelete = null },
        onDismiss = { pendingDelete = null },
    ) }
}

@Composable
private fun AddTransactionDialog(accounts: List<Account>, categories: List<Category>, subcategories: List<Subcategory>, onDismiss: () -> Unit, onSave: (TransactionType, Long, String, String?, String?, String?, String) -> Unit) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var account by remember { mutableStateOf(accounts.first().id) }
    var destination by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf<String?>(null) }
    var subcategory by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    val eligible = categories.filter { it.kind.name == type.name }
    LaunchedEffect(type) { category = null; subcategory = null; destination = null }
    val parsed = parseMinor(amount)
    val amountError = amount.isNotEmpty() && (parsed == null || parsed <= 0)
    val selectionValid = if (type == TransactionType.TRANSFER) destination != null && destination != account else category != null
    val valid = parsed != null && parsed > 0 && selectionValid
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.transactions_add_title)) },
        text = { LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { TransactionType.entries.forEach { FilterChip(type == it, { type = it }, { Text(typeLabel(it)) }) } } }
            item { BudgetAmountField(amount, { amount = it }, stringResource(R.string.transactions_amount), stringResource(R.string.transactions_currency), isError = amountError, supportingText = if (amountError) stringResource(R.string.transactions_amount_error) else null) }
            item { SimpleSelector(stringResource(R.string.transactions_account), accounts, account, { it.name }) { account = it.id; if (destination == it.id) destination = null } }
            if (type == TransactionType.TRANSFER) item { SimpleSelector(stringResource(R.string.transactions_destination), accounts.filter { it.id != account }, destination, { it.name }) { destination = it.id } }
            else item {
                SimpleSelector(stringResource(R.string.transactions_category), eligible, category, { categoryText(it) }) { category = it.id; subcategory = null }
                val eligibleSubs = subcategories.filter { it.categoryId == category }
                if (eligibleSubs.isNotEmpty()) SimpleSelector(stringResource(R.string.transactions_subcategory), eligibleSubs, subcategory, { it.name }) { subcategory = it.id }
                if (eligible.isEmpty()) Text(stringResource(R.string.transactions_no_category), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            item { BudgetTextField(description, { if (it.length <= 120) description = it }, stringResource(R.string.transactions_description), supportingText = stringResource(R.string.transactions_description_limit, description.length)) }
        } },
        confirmButton = { TextButton(onClick = { onSave(type, parsed!!, account, destination, category, subcategory, description) }, enabled = valid) { Text(stringResource(R.string.transactions_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.transactions_cancel)) } },
    )
}

@Composable
private fun <T : Any> SimpleSelector(label: String, values: List<T>, selectedId: String?, text: @Composable (T) -> String, select: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selected = values.firstOrNull { item -> when(item) { is Account -> item.id == selectedId; is Category -> item.id == selectedId; is Subcategory -> item.id == selectedId; else -> false } }
    Box { OutlinedButton({ expanded = true }, Modifier.fillMaxWidth(), enabled = values.isNotEmpty()) { Text(selected?.let { text(it) } ?: label) }; DropdownMenu(expanded, { expanded = false }) { values.forEach { item -> DropdownMenuItem({ Text(text(item)) }, { select(item); expanded = false }) } } }
}
@Composable private fun categoryText(category: Category) = category.customName ?: category.nameKey?.let { transactionCategory(it) } ?: stringResource(R.string.transaction_other)
@Composable private fun transactionCategory(key: String) = stringResource(when(key) { "category_food" -> R.string.transaction_food; "category_transport" -> R.string.transaction_transport; "category_housing" -> R.string.transaction_housing; "category_health" -> R.string.transaction_health; "category_leisure" -> R.string.transaction_leisure; "category_utilities" -> R.string.transaction_utilities; "category_education" -> R.string.transaction_education; "category_family" -> R.string.transaction_family; "category_clothing" -> R.string.transaction_clothing; "category_taxes" -> R.string.transaction_taxes; "category_salary" -> R.string.transaction_salary; "category_freelance" -> R.string.transaction_freelance; "category_pension" -> R.string.transaction_pension; "category_benefits" -> R.string.transaction_benefits; "category_gift" -> R.string.transaction_gift; else -> R.string.transaction_other })
@Composable private fun typeLabel(type: TransactionType) = stringResource(when(type) { TransactionType.EXPENSE -> R.string.transaction_expense; TransactionType.INCOME -> R.string.transaction_income; TransactionType.TRANSFER -> R.string.transaction_transfer })
private fun visualType(type: TransactionType) = when(type) { TransactionType.EXPENSE -> TransactionVisualType.Expense; TransactionType.INCOME -> TransactionVisualType.Income; TransactionType.TRANSFER -> TransactionVisualType.Transfer }
internal fun parseMinor(value: String): Long? { val p=value.trim().replace(',','.').split('.'); if(p.size>2||p.any{part->part.any{!it.isDigit()}})return null; val major=p[0].ifEmpty{"0"}.toLongOrNull()?:return null; val minor=p.getOrNull(1).orEmpty(); if(minor.length>2)return null; return runCatching{Math.addExact(Math.multiplyExact(major,100),minor.padEnd(2,'0').ifEmpty{"0"}.toLong())}.getOrNull() }
