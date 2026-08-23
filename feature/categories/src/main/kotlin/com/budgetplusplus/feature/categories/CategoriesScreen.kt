package com.budgetplusplus.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.budgetplusplus.core.designsystem.components.BudgetEmptyState
import com.budgetplusplus.core.designsystem.components.BudgetSearchBar
import com.budgetplusplus.core.designsystem.components.BudgetTextField
import com.budgetplusplus.core.designsystem.components.BudgetTopAppBar
import com.budgetplusplus.core.designsystem.icons.CategoryIconCatalog
import com.budgetplusplus.core.model.Category
import com.budgetplusplus.core.model.CategoryKind
import com.budgetplusplus.core.model.CropMode
import com.budgetplusplus.core.model.Subcategory
import com.budgetplusplus.domain.repository.CategoryRepository
import com.budgetplusplus.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CategoriesViewModel @Inject constructor(private val repository: CategoryRepository, private val mediaRepository: MediaRepository) : ViewModel() {
    val categories = repository.observeCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val subcategories = repository.observeSubcategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _hasError = MutableStateFlow(false); val hasError = _hasError.asStateFlow()
    private fun action(block: suspend () -> Unit, done: () -> Unit = {}) = viewModelScope.launch { runCatching { block() }.onSuccess { _hasError.value = false; done() }.onFailure { _hasError.value = true } }
    fun add(name: String, kind: CategoryKind, icon: String, color: String, done: () -> Unit) = action({ repository.create(name, kind, icon, color) }, done)
    fun update(category: Category, name: String, icon: String, color: String, done: () -> Unit) = action({ repository.update(category.id, name, icon, color) }, done)
    fun addSub(categoryId: String, name: String, done: () -> Unit) = action({ repository.createSubcategory(categoryId, name) }, done)
    fun updateSub(value: Subcategory, name: String, done: () -> Unit) = action({ repository.updateSubcategory(value.id, name) }, done)
    fun archive(category: Category, replacement: String?, done: () -> Unit) = action({ repository.setArchived(category.id, !category.isArchived, replacement) }, done)
    fun archiveSub(value: Subcategory, replacement: String?, done: () -> Unit) = action({ repository.setSubcategoryArchived(value.id, !value.isArchived, replacement) }, done)
    fun delete(category: Category, replacement: String?, done: () -> Unit) = action({ repository.delete(category.id, replacement) }, done)
    fun media(categoryId:String,uri:String,crop:CropMode,done:()->Unit)=action({val asset=mediaRepository.importMedia(uri,crop);mediaRepository.attachToCategory(categoryId,asset.id)},done)
    fun clearError() { _hasError.value = false }
}

private enum class CategoryAction { EDIT, ADD_SUBCATEGORY, ARCHIVE, DELETE }
private data class PendingCategoryAction(val category: Category, val action: CategoryAction)

@Composable
fun CategoriesScreen(onBack: (() -> Unit)?, viewModel: CategoriesViewModel = hiltViewModel()) {
    val categories by viewModel.categories.collectAsStateWithLifecycle(); val subcategories by viewModel.subcategories.collectAsStateWithLifecycle(); val hasError by viewModel.hasError.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }; var selectedKind by remember { mutableStateOf(CategoryKind.EXPENSE) }; var showArchived by remember { mutableStateOf(false) }; var expandedId by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<Category?>(null) }; var showCreate by remember { mutableStateOf(false) }; var subEditor by remember { mutableStateOf<Pair<String, Subcategory? >?>(null) }; var pending by remember { mutableStateOf<PendingCategoryAction?>(null) }; var pendingSub by remember { mutableStateOf<Subcategory?>(null) }
    val snackbar = remember { SnackbarHostState() }; val error = stringResource(R.string.categories_error)
    LaunchedEffect(hasError) { if (hasError) { snackbar.showSnackbar(error); viewModel.clearError() } }
    val labeled = categories.map { it to categoryLabel(it) }
    val visible = labeled.filter { (category, label) -> category.kind == selectedKind && category.isArchived == showArchived && (query.isBlank() || label.contains(query, true) || subcategories.any { it.categoryId == category.id && it.name.contains(query, true) }) }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, topBar = { BudgetTopAppBar(stringResource(R.string.categories_title), onBackClick = onBack) }, floatingActionButton = { FloatingActionButton({ showCreate = true }) { Text(stringResource(R.string.categories_add_symbol)) } }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            BudgetSearchBar(query, { query = it }, Modifier.padding(horizontal = 16.dp), stringResource(R.string.categories_search))
            LazyRow(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(CategoryKind.entries) { kind -> FilterChip(selectedKind == kind, { selectedKind = kind }, { Text(kindLabel(kind)) }) }; item { FilterChip(showArchived, { showArchived = !showArchived }, { Text(stringResource(R.string.categories_archived)) }) } }
            if (visible.isEmpty()) BudgetEmptyState(stringResource(R.string.categories_empty), stringResource(R.string.categories_empty_message), Modifier.fillMaxSize(), if (!showArchived) stringResource(R.string.categories_add_action) else null, if (!showArchived) ({ showCreate = true }) else null)
            else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(visible, key = { it.first.id }) { (category, label) ->
                CategoryItem(category, label, subcategories.filter { it.categoryId == category.id }, expandedId == category.id, { expandedId = if (expandedId == category.id) null else category.id }, { action -> when(action) { CategoryAction.EDIT -> editing = category; CategoryAction.ADD_SUBCATEGORY -> subEditor = category.id to null; else -> pending = PendingCategoryAction(category, action) } }, { subEditor = category.id to it }, { pendingSub = it })
            } }
        }
    }
    if (showCreate) CategoryEditorDialog(null, selectedKind, { showCreate = false }, null) { n,k,i,c -> viewModel.add(n,k,i,c) { showCreate = false } }
    editing?.let { value -> CategoryEditorDialog(value, value.kind, { editing = null }, {uri,crop->viewModel.media(value.id,uri,crop){}}) { n,_,i,c -> viewModel.update(value,n,i,c) { editing = null } } }
    subEditor?.let { (parent, value) -> SubcategoryEditorDialog(value, { subEditor = null }) { name -> if (value == null) viewModel.addSub(parent,name) { subEditor = null } else viewModel.updateSub(value,name) { subEditor = null } } }
    pending?.let { request -> CategorySafetyDialog(request, categories, { pending = null }) { replacement -> when(request.action) { CategoryAction.ARCHIVE -> viewModel.archive(request.category,replacement) { pending = null }; CategoryAction.DELETE -> viewModel.delete(request.category,replacement) { pending = null }; else -> Unit } } }
    pendingSub?.let { value -> SubcategorySafetyDialog(value, subcategories.filter { it.categoryId == value.categoryId && it.id != value.id && !it.isArchived }, { pendingSub = null }) { replacement -> viewModel.archiveSub(value,replacement) { pendingSub = null } } }
}

@Composable private fun CategoryItem(category: Category, label: String, children: List<Subcategory>, expanded: Boolean, toggle: () -> Unit, action: (CategoryAction) -> Unit, editSub: (Subcategory) -> Unit, archiveSub: (Subcategory) -> Unit) {
    var menu by remember { mutableStateOf(false) }; ElevatedCard(Modifier.fillMaxWidth()) { Column { ListItem(
        modifier = Modifier.clickable(onClick = toggle),
        leadingContent = { Box(Modifier.size(40.dp).background(categoryColor(category.colorKey), CircleShape), contentAlignment = Alignment.Center) { Icon(categoryIcon(CategoryIconCatalog.effectiveKey(category.iconKey, category.nameKey)), null, tint = Color.White) } },
        headlineContent = { Text(label) }, supportingContent = { Text(stringResource(R.string.categories_usage, category.usageCount, children.count { !it.isArchived })) },
        trailingContent = { Box { TextButton({ menu = true }) { Text(stringResource(R.string.categories_actions)) }; DropdownMenu(menu,{menu=false}) { DropdownMenuItem({Text(stringResource(R.string.categories_edit))},{menu=false;action(CategoryAction.EDIT)}); DropdownMenuItem({Text(stringResource(R.string.categories_add_subcategory))},{menu=false;action(CategoryAction.ADD_SUBCATEGORY)}); DropdownMenuItem({Text(stringResource(if(category.isArchived) R.string.categories_restore else R.string.categories_archive))},{menu=false;action(CategoryAction.ARCHIVE)}); DropdownMenuItem({Text(stringResource(R.string.categories_delete))},{menu=false;action(CategoryAction.DELETE)}) } } }
    ); if (expanded) children.forEach { sub -> ListItem(modifier=Modifier.padding(start = 28.dp), headlineContent={Text(sub.name)}, supportingContent={Text(stringResource(R.string.categories_operation_count,sub.usageCount))}, trailingContent={ Row { TextButton({editSub(sub)}){Text(stringResource(R.string.categories_edit))}; TextButton({archiveSub(sub)}){Text(stringResource(if(sub.isArchived) R.string.categories_restore else R.string.categories_archive))} } }) } } }
}

@Composable private fun CategoryEditorDialog(value: Category?, initialKind: CategoryKind, dismiss:()->Unit, media:((String,CropMode)->Unit)?, save:(String,CategoryKind,String,String)->Unit){ val initialName=value?.let{categoryLabel(it)}.orEmpty();var name by remember(value?.id){mutableStateOf(initialName)}; var kind by remember{mutableStateOf(initialKind)}; var icon by remember(value?.id){mutableStateOf(value?.let{CategoryIconCatalog.effectiveKey(it.iconKey,it.nameKey)}?:"category")}; var color by remember{mutableStateOf(value?.colorKey?:"primary")}; AlertDialog(onDismissRequest=dismiss,title={Text(stringResource(if(value==null)R.string.categories_add_title else R.string.categories_edit_title))},text={LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp)){item{BudgetTextField(name,{name=it},stringResource(R.string.categories_name))};if(value==null)item{Row{CategoryKind.entries.forEach{FilterChip(kind==it,{kind=it},{Text(kindLabel(it))},Modifier.padding(end=6.dp))}}};item{Text(stringResource(R.string.categories_icon));LazyRow{items(iconOptions){key->FilterChip(icon==key,{icon=key},{Icon(categoryIcon(key),stringResource(R.string.categories_icon_category))},Modifier.padding(end=6.dp))}}};if(media!=null)item{com.budgetplusplus.core.ui.MediaPickerButton(media)};item{Text(stringResource(R.string.categories_color));LazyRow{items(colorOptions){key->FilterChip(color==key,{color=key},{Box(Modifier.size(24.dp).background(categoryColor(key),CircleShape))},Modifier.padding(end=6.dp))}}}}},confirmButton={TextButton({save(name.trim(),kind,icon,color)},enabled=name.isNotBlank()&&name.length<=60){Text(stringResource(R.string.categories_save))}},dismissButton={TextButton(dismiss){Text(stringResource(R.string.categories_cancel))}}) }
@Composable private fun SubcategoryEditorDialog(value:Subcategory?,dismiss:()->Unit,save:(String)->Unit){var name by remember{mutableStateOf(value?.name.orEmpty())};AlertDialog(onDismissRequest=dismiss,title={Text(stringResource(if(value==null)R.string.categories_new_subcategory else R.string.categories_edit_subcategory))},text={BudgetTextField(name,{name=it},stringResource(R.string.categories_name))},confirmButton={TextButton({save(name.trim())},enabled=name.isNotBlank()&&name.length<=60){Text(stringResource(R.string.categories_save))}},dismissButton={TextButton(dismiss){Text(stringResource(R.string.categories_cancel))}})}

@Composable private fun CategorySafetyDialog(request:PendingCategoryAction,categories:List<Category>,dismiss:()->Unit,confirm:(String?)->Unit){val source=request.category;val choices=categories.filter{it.id!=source.id&&it.kind==source.kind&&!it.isArchived};var replacement by remember{mutableStateOf<String?>(null)};val needs=source.usageCount>0 && (request.action==CategoryAction.DELETE || !source.isArchived);AlertDialog(onDismissRequest=dismiss,title={Text(stringResource(if(request.action==CategoryAction.DELETE)R.string.categories_delete_title else if(source.isArchived)R.string.categories_restore_title else R.string.categories_archive_title))},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text(stringResource(if(needs)R.string.categories_reassign_required else R.string.categories_safe_action,source.usageCount));if(needs)choices.forEach{c->FilterChip(replacement==c.id,{replacement=c.id},{Text(categoryLabel(c))})}}},confirmButton={TextButton({confirm(replacement)},enabled=!needs||replacement!=null){Text(stringResource(if(request.action==CategoryAction.DELETE)R.string.categories_delete else if(source.isArchived)R.string.categories_restore else R.string.categories_archive))}},dismissButton={TextButton(dismiss){Text(stringResource(R.string.categories_cancel))}})}
@Composable private fun SubcategorySafetyDialog(value:Subcategory,choices:List<Subcategory>,dismiss:()->Unit,confirm:(String?)->Unit){var replacement by remember{mutableStateOf<String?>(null)};val needs=value.usageCount>0 && !value.isArchived;AlertDialog(onDismissRequest=dismiss,title={Text(stringResource(R.string.categories_archive_subcategory))},text={Column{Text(stringResource(if(needs)R.string.categories_reassign_sub_required else R.string.categories_safe_sub_action,value.usageCount));choices.forEach{c->FilterChip(replacement==c.id,{replacement=c.id},{Text(c.name)})}}},confirmButton={TextButton({confirm(replacement)},enabled=!needs||replacement!=null){Text(stringResource(if(value.isArchived)R.string.categories_restore else R.string.categories_archive))}},dismissButton={TextButton(dismiss){Text(stringResource(R.string.categories_cancel))}})}

private val iconOptions=CategoryIconCatalog.keys;private val colorOptions=listOf("primary","green","orange","blue","purple","red")
private fun categoryIcon(key:String):ImageVector=CategoryIconCatalog.icon(key)
private fun categoryColor(key:String):Color=when(key){"green"->Color(0xFF2E7D32);"orange"->Color(0xFFEF6C00);"blue"->Color(0xFF1565C0);"purple"->Color(0xFF6A1B9A);"red"->Color(0xFFC62828);else->Color(0xFF386A54)}
@Composable fun categoryLabel(category:Category):String=category.customName?:category.nameKey?.let{systemCategoryLabel(it)}?:stringResource(R.string.category_unknown)
@Composable fun systemCategoryLabel(key:String):String=stringResource(when(key){"category_food"->R.string.category_food;"category_transport"->R.string.category_transport;"category_housing"->R.string.category_housing;"category_health"->R.string.category_health;"category_leisure"->R.string.category_leisure;"category_utilities"->R.string.category_utilities;"category_education"->R.string.category_education;"category_family"->R.string.category_family;"category_clothing"->R.string.category_clothing;"category_taxes"->R.string.category_taxes;"category_salary"->R.string.category_salary;"category_freelance"->R.string.category_freelance;"category_pension"->R.string.category_pension;"category_benefits"->R.string.category_benefits;"category_gift"->R.string.category_gift;"category_other_income"->R.string.category_other_income;else->R.string.category_unknown})
@Composable private fun kindLabel(kind:CategoryKind)=stringResource(if(kind==CategoryKind.EXPENSE)R.string.categories_expense else R.string.categories_income)
