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
import com.budgetplusplus.core.designsystem.components.BudgetEmptyState
import com.budgetplusplus.core.designsystem.components.BudgetTextField
import com.budgetplusplus.core.designsystem.components.BudgetTopAppBar
import com.budgetplusplus.core.model.Category
import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CategoriesViewModel @Inject constructor(private val repository: CategoryRepository) : ViewModel() {
    val categories = repository.observeCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun add(name: String, kind: CategoryKind) = viewModelScope.launch { repository.create(name, kind) }
    fun archive(category: Category) = viewModelScope.launch { repository.setArchived(category.id, !category.isArchived) }
}

@Composable
fun CategoriesScreen(onBack: () -> Unit, viewModel: CategoriesViewModel = hiltViewModel()) {
    val categories by viewModel.categories.collectAsStateWithLifecycle(); var add by remember { mutableStateOf(false) }
    Scaffold(topBar = { BudgetTopAppBar(stringResource(R.string.categories_title), onBackClick = onBack) }, floatingActionButton = { FloatingActionButton(onClick = { add = true }) { Text(stringResource(R.string.categories_add_symbol)) } }) { padding ->
        if (categories.isEmpty()) BudgetEmptyState(stringResource(R.string.categories_empty), stringResource(R.string.categories_empty_message), Modifier.padding(padding))
        else LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories, key = { it.id }) { category -> ListItem(headlineContent = { Text(categoryLabel(category)) }, supportingContent = { Text(kindLabel(category.kind)) }, trailingContent = { if (!category.isSystem) TextButton(onClick = { viewModel.archive(category) }) { Text(stringResource(if (category.isArchived) R.string.categories_restore else R.string.categories_archive)) } }); HorizontalDivider() }
        }
    }
    if (add) AddCategoryDialog({ add = false }) { name, kind -> viewModel.add(name, kind); add = false }
}

@Composable private fun AddCategoryDialog(onDismiss: () -> Unit, onSave: (String, CategoryKind) -> Unit) {
    var name by remember { mutableStateOf("") }; var kind by remember { mutableStateOf(CategoryKind.EXPENSE) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.categories_add_title)) }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { BudgetTextField(name, { name = it }, stringResource(R.string.categories_name)); Row { CategoryKind.entries.forEach { item -> FilterChip(item == kind, { kind = item }, { Text(kindLabel(item)) }, modifier = Modifier.padding(end = 8.dp)) } } } }, confirmButton = { TextButton(onClick = { onSave(name, kind) }, enabled = name.isNotBlank()) { Text(stringResource(R.string.categories_save)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.categories_cancel)) } })
}

@Composable fun categoryLabel(category: Category): String = category.customName ?: category.nameKey?.let { key -> systemCategoryLabel(key) } ?: stringResource(R.string.category_unknown)
@Composable fun systemCategoryLabel(key: String): String = stringResource(when (key) { "category_food" -> R.string.category_food; "category_transport" -> R.string.category_transport; "category_housing" -> R.string.category_housing; "category_health" -> R.string.category_health; "category_leisure" -> R.string.category_leisure; "category_salary" -> R.string.category_salary; "category_gift" -> R.string.category_gift; "category_other_income" -> R.string.category_other_income; else -> R.string.category_unknown })
@Composable private fun kindLabel(kind: CategoryKind) = stringResource(if (kind == CategoryKind.EXPENSE) R.string.categories_expense else R.string.categories_income)
