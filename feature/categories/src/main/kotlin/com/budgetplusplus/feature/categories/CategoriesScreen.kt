package com.budgetplusplus.feature.categories

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
import com.budgetplusplus.core.designsystem.components.BudgetConfirmationDialog
import com.budgetplusplus.core.designsystem.components.BudgetEmptyState
import com.budgetplusplus.core.designsystem.components.BudgetTextField
import com.budgetplusplus.core.designsystem.components.BudgetTopAppBar
import com.budgetplusplus.core.model.Category
import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CategoriesViewModel @Inject constructor(private val repository: CategoryRepository) : ViewModel() {
    val categories = repository.observeCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _hasError = MutableStateFlow(false)
    val hasError = _hasError.asStateFlow()
    fun add(name: String, kind: CategoryKind, onSuccess: () -> Unit) = viewModelScope.launch { runCatching { repository.create(name, kind) }.onSuccess { _hasError.value = false; onSuccess() }.onFailure { _hasError.value = true } }
    fun archive(category: Category) = viewModelScope.launch { runCatching { repository.setArchived(category.id, !category.isArchived) }.onSuccess { _hasError.value = false }.onFailure { _hasError.value = true } }
    fun clearError() { _hasError.value = false }
}

@Composable
fun CategoriesScreen(onBack: (() -> Unit)?, viewModel: CategoriesViewModel = hiltViewModel()) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val hasError by viewModel.hasError.collectAsStateWithLifecycle()
    var add by remember { mutableStateOf(false) }
    var selectedKind by remember { mutableStateOf(CategoryKind.EXPENSE) }
    var showArchived by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<Category?>(null) }
    val visible = categories.filter { it.kind == selectedKind && it.isArchived == showArchived }
    val snackbar = remember { SnackbarHostState() }
    val error = stringResource(R.string.categories_error)
    LaunchedEffect(hasError) { if (hasError) { snackbar.showSnackbar(error); viewModel.clearError() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { BudgetTopAppBar(stringResource(R.string.categories_title), onBackClick = onBack) },
        floatingActionButton = { FloatingActionButton(onClick = { add = true }) { Text(stringResource(R.string.categories_add_symbol)) } },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryKind.entries.forEach { kind -> FilterChip(selectedKind == kind, { selectedKind = kind }, { Text(kindLabel(kind)) }) }
            }
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(!showArchived, { showArchived = false }, { Text(stringResource(R.string.categories_active)) })
                FilterChip(showArchived, { showArchived = true }, { Text(stringResource(R.string.categories_archived)) })
            }
            if (visible.isEmpty()) {
                BudgetEmptyState(
                    title = stringResource(if (showArchived) R.string.categories_no_archived else R.string.categories_empty),
                    message = stringResource(if (showArchived) R.string.categories_no_archived_message else R.string.categories_empty_message),
                    actionText = if (showArchived) null else stringResource(R.string.categories_add_action),
                    onAction = if (showArchived) null else ({ add = true }),
                    modifier = Modifier.fillMaxSize(),
                )
            } else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                items(visible, key = { it.id }) { category ->
                    ListItem(
                        headlineContent = { Text(categoryLabel(category)) },
                        supportingContent = { Text(stringResource(if (category.isSystem) R.string.categories_system else R.string.categories_custom)) },
                        trailingContent = { if (!category.isSystem) TextButton(onClick = { pending = category }) { Text(stringResource(if (category.isArchived) R.string.categories_restore else R.string.categories_archive)) } },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
    if (add) AddCategoryDialog({ add = false }) { name, kind -> viewModel.add(name, kind) { add = false } }
    pending?.let { category -> BudgetConfirmationDialog(
        title = stringResource(if (category.isArchived) R.string.categories_restore_title else R.string.categories_archive_title),
        message = stringResource(if (category.isArchived) R.string.categories_restore_message else R.string.categories_archive_message, categoryLabel(category)),
        confirmText = stringResource(if (category.isArchived) R.string.categories_restore else R.string.categories_archive),
        destructive = !category.isArchived,
        onConfirm = { viewModel.archive(category); pending = null },
        onDismiss = { pending = null },
    ) }
}

@Composable
private fun AddCategoryDialog(onDismiss: () -> Unit, onSave: (String, CategoryKind) -> Unit) {
    var name by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(CategoryKind.EXPENSE) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.categories_add_title)) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BudgetTextField(name, { name = it }, stringResource(R.string.categories_name), isError = name.length > 60, supportingText = if (name.length > 60) stringResource(R.string.categories_name_error) else null)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { CategoryKind.entries.forEach { item -> FilterChip(item == kind, { kind = item }, { Text(kindLabel(item)) }) } }
        } },
        confirmButton = { TextButton(onClick = { onSave(name.trim(), kind) }, enabled = name.isNotBlank() && name.length <= 60) { Text(stringResource(R.string.categories_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.categories_cancel)) } },
    )
}

@Composable fun categoryLabel(category: Category): String = category.customName ?: category.nameKey?.let { systemCategoryLabel(it) } ?: stringResource(R.string.category_unknown)
@Composable fun systemCategoryLabel(key: String): String = stringResource(when (key) { "category_food" -> R.string.category_food; "category_transport" -> R.string.category_transport; "category_housing" -> R.string.category_housing; "category_health" -> R.string.category_health; "category_leisure" -> R.string.category_leisure; "category_salary" -> R.string.category_salary; "category_gift" -> R.string.category_gift; "category_other_income" -> R.string.category_other_income; else -> R.string.category_unknown })
@Composable private fun kindLabel(kind: CategoryKind) = stringResource(if (kind == CategoryKind.EXPENSE) R.string.categories_expense else R.string.categories_income)
