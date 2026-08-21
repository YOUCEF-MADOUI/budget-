package com.budgetplusplus.feature.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    val categories = categoriesRepository.observeCategories().map { it.filterNot(Category::isArchived) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val transactions = repository.observeTransactions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun add(type: TransactionType, amount: Long, account: String, destination: String?, category: String?, description: String) = viewModelScope.launch { repository.create(type, amount, account, destination, category, description) }
    fun delete(id: String) = viewModelScope.launch { repository.delete(id) }
}

@Composable
fun TransactionsScreen(onBack: () -> Unit, viewModel: TransactionsViewModel = hiltViewModel()) {
    val rows by viewModel.transactions.collectAsStateWithLifecycle(); val accounts by viewModel.accounts.collectAsStateWithLifecycle(); val categories by viewModel.categories.collectAsStateWithLifecycle(); var add by remember { mutableStateOf(false) }
    Scaffold(topBar = { BudgetTopAppBar(stringResource(R.string.transactions_title), onBackClick = onBack) }, floatingActionButton = { FloatingActionButton(onClick = { add = true }, enabled = accounts.isNotEmpty()) { Text(stringResource(R.string.transactions_add_symbol)) } }) { padding ->
        if (rows.isEmpty()) BudgetEmptyState(stringResource(R.string.transactions_empty), stringResource(if (accounts.isEmpty()) R.string.transactions_need_account else R.string.transactions_empty_message), Modifier.padding(padding))
        else LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { items(rows, key = { it.id }) { transaction ->
            TransactionRow(category = transaction.categoryCustomName ?: transaction.categoryNameKey?.let { transactionCategory(it) } ?: typeLabel(transaction.type), description = transaction.description, date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(transaction.occurredAt)), account = if (transaction.destinationAccountName == null) transaction.accountName else stringResource(R.string.transactions_transfer_accounts, transaction.accountName, transaction.destinationAccountName), amountMinor = transaction.amountMinor, currencyCode = transaction.currencyCode, type = visualType(transaction.type), categoryIcon = BudgetIcons.Category, onClick = { viewModel.delete(transaction.id) })
        } }
    }
    if (add) AddTransactionDialog(accounts, categories, { add = false }) { type, amount, account, destination, category, description -> viewModel.add(type, amount, account, destination, category, description); add = false }
}

@Composable private fun AddTransactionDialog(accounts: List<Account>, categories: List<Category>, onDismiss: () -> Unit, onSave: (TransactionType, Long, String, String?, String?, String) -> Unit) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }; var amount by remember { mutableStateOf("") }; var account by remember { mutableStateOf(accounts.first().id) }; var destination by remember { mutableStateOf<String?>(null) }; var category by remember { mutableStateOf<String?>(null) }; var description by remember { mutableStateOf("") }
    val eligible = categories.filter { it.kind.name == type.name }; LaunchedEffect(type) { category = null; destination = null }
    val parsed = parseMinor(amount); val valid = parsed != null && parsed > 0 && if (type == TransactionType.TRANSFER) destination != null && destination != account else category != null
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.transactions_add_title)) }, text = { LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { TransactionType.entries.forEach { FilterChip(type == it, { type = it }, { Text(typeLabel(it)) }) } } }
        item { BudgetAmountField(amount, { amount = it }, stringResource(R.string.transactions_amount), stringResource(R.string.transactions_currency)) }
        item { SimpleSelector(stringResource(R.string.transactions_account), accounts, account, { it.name }) { account = it.id } }
        if (type == TransactionType.TRANSFER) item { SimpleSelector(stringResource(R.string.transactions_destination), accounts.filter { it.id != account }, destination, { it.name }) { destination = it.id } }
        else item { SimpleSelector(stringResource(R.string.transactions_category), eligible, category, { categoryText(it) }) { category = it.id } }
        item { BudgetTextField(description, { description = it }, stringResource(R.string.transactions_description)) }
    } }, confirmButton = { TextButton(onClick = { onSave(type, parsed!!, account, destination, category, description) }, enabled = valid) { Text(stringResource(R.string.transactions_save)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.transactions_cancel)) } })
}

@Composable private fun <T> SimpleSelector(label: String, values: List<T>, selectedId: String?, text: @Composable (T) -> String, select: (T) -> Unit) where T : Any {
    var expanded by remember { mutableStateOf(false) }; val selected = values.firstOrNull { item -> when(item) { is Account -> item.id == selectedId; is Category -> item.id == selectedId; else -> false } }
    Box { OutlinedButton({ expanded = true }, Modifier.fillMaxWidth()) { Text(selected?.let { text(it) } ?: label) }; DropdownMenu(expanded, { expanded = false }) { values.forEach { item -> DropdownMenuItem({ Text(text(item)) }, { select(item); expanded = false }) } } }
}
@Composable private fun categoryText(category: Category) = category.customName ?: category.nameKey?.let { transactionCategory(it) } ?: stringResource(R.string.transaction_other)
@Composable private fun transactionCategory(key: String) = stringResource(when(key) { "category_food" -> R.string.transaction_food; "category_transport" -> R.string.transaction_transport; "category_housing" -> R.string.transaction_housing; "category_health" -> R.string.transaction_health; "category_leisure" -> R.string.transaction_leisure; "category_salary" -> R.string.transaction_salary; "category_gift" -> R.string.transaction_gift; else -> R.string.transaction_other })
@Composable private fun typeLabel(type: TransactionType) = stringResource(when(type) { TransactionType.EXPENSE -> R.string.transaction_expense; TransactionType.INCOME -> R.string.transaction_income; TransactionType.TRANSFER -> R.string.transaction_transfer })
private fun visualType(type: TransactionType) = when(type) { TransactionType.EXPENSE -> TransactionVisualType.Expense; TransactionType.INCOME -> TransactionVisualType.Income; TransactionType.TRANSFER -> TransactionVisualType.Transfer }
internal fun parseMinor(value: String): Long? { val p=value.trim().replace(',','.').split('.'); if(p.size>2||p.any{part->part.any{!it.isDigit()}})return null; val major=p[0].ifEmpty{"0"}.toLongOrNull()?:return null; val minor=p.getOrNull(1).orEmpty(); if(minor.length>2)return null; return runCatching{Math.addExact(Math.multiplyExact(major,100),minor.padEnd(2,'0').ifEmpty{"0"}.toLong())}.getOrNull() }
