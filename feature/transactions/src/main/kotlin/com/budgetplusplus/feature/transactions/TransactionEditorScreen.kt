package com.budgetplusplus.feature.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.budgetplusplus.core.designsystem.components.*
import com.budgetplusplus.core.designsystem.icons.BudgetIcons
import com.budgetplusplus.core.model.*
import com.budgetplusplus.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel class TransactionEditorViewModel @Inject constructor(private val transactions:TransactionRepository,private val categoryRepository:CategoryRepository,accountsRepository:AccountRepository,private val mediaRepository:MediaRepository):ViewModel(){
 val accounts=accountsRepository.observeAccounts().map{it.filterNot(Account::isArchived)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList());val categories=categoryRepository.observeCategories().map{it.filterNot(Category::isArchived)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList());val subcategories=categoryRepository.observeSubcategories().map{it.filterNot(Subcategory::isArchived)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList());private val _existing=MutableStateFlow<FinanceTransaction?>(null);val existing=_existing.asStateFlow();private val _error=MutableStateFlow(false);val error=_error.asStateFlow()
 fun load(id:String){if(_existing.value?.id==id)return;viewModelScope.launch{runCatching{requireNotNull(transactions.get(id))}.onSuccess{_existing.value=it}.onFailure{_error.value=true}}}
 fun save(id:String?,type:TransactionType,amount:Long,account:String,destination:String?,category:String?,subcategory:String?,date:String,description:String,mediaId:String?,done:()->Unit)=viewModelScope.launch{runCatching{val transactionId=if(id==null)transactions.create(type,amount,account,destination,category,description,subcategory,localDate=date)else{id.also{transactions.update(it,type,amount,account,destination,category,description,subcategory,date)}};if(mediaId!=null)mediaRepository.attachToTransaction(transactionId,mediaId)}.onSuccess{done()}.onFailure{_error.value=true}}
 fun importMedia(uri:String,crop:CropMode,done:(String)->Unit)=viewModelScope.launch{runCatching{mediaRepository.importMedia(uri,crop)}.onSuccess{done(it.id)}.onFailure{_error.value=true}}
 fun quickCategory(name:String,kind:CategoryKind,icon:String,color:String,subcategory:String,done:(String,String?)->Unit)=viewModelScope.launch{runCatching{val id=categoryRepository.create(name,kind,icon,color);val sub=if(subcategory.isBlank())null else categoryRepository.createSubcategory(id,subcategory);id to sub}.onSuccess{done(it.first,it.second)}.onFailure{_error.value=true}}
 fun clearError(){_error.value=false}
}

@Composable fun TransactionEditorScreen(transactionId:String?,onBack:()->Unit,initialType:TransactionType?=null,initialAccountId:String?=null,vm:TransactionEditorViewModel=hiltViewModel()){val existing by vm.existing.collectAsStateWithLifecycle();val accounts by vm.accounts.collectAsStateWithLifecycle();val categories by vm.categories.collectAsStateWithLifecycle();val subs by vm.subcategories.collectAsStateWithLifecycle();val error by vm.error.collectAsStateWithLifecycle();val snackbar=remember{SnackbarHostState()};val errorMessage=stringResource(R.string.transaction_editor_error);LaunchedEffect(transactionId){if(transactionId!=null)vm.load(transactionId)};LaunchedEffect(error){if(error){snackbar.showSnackbar(errorMessage);vm.clearError()}}
 Scaffold(snackbarHost={SnackbarHost(snackbar)},topBar={BudgetTopAppBar(stringResource(if(transactionId==null)R.string.transaction_editor_new else R.string.transaction_editor_edit),onBackClick=onBack)}){padding->if(transactionId!=null&&existing==null)BudgetLoadingIndicator(Modifier.padding(padding).fillMaxSize())else key(existing?.id){EditorForm(existing,initialType,initialAccountId,accounts,categories,subs,onBack,{id,type,amount,account,destination,category,sub,date,description,media->vm.save(id,type,amount,account,destination,category,sub,date,description,media,onBack)},vm::quickCategory,vm::importMedia,Modifier.padding(padding))}}
}

@Composable private fun EditorForm(initial:FinanceTransaction?,initialType:TransactionType?,initialAccountId:String?,accounts:List<Account>,categories:List<Category>,subs:List<Subcategory>,cancel:()->Unit,save:(String?,TransactionType,Long,String,String?,String?,String?,String,String,String?)->Unit,quick:(String,CategoryKind,String,String,String,(String,String?)->Unit)->Unit,importMedia:(String,CropMode,(String)->Unit)->Unit,modifier:Modifier){var typeName by rememberSaveable{mutableStateOf(initial?.type?.name?:initialType?.name?:TransactionType.EXPENSE.name)};val type=TransactionType.valueOf(typeName);var amount by rememberSaveable{mutableStateOf(initial?.amountMinor?.let(::formatMinor).orEmpty())};var account by rememberSaveable{mutableStateOf(initial?.accountId?:initialAccountId?:accounts.firstOrNull()?.id.orEmpty())};var destination by rememberSaveable{mutableStateOf(initial?.destinationAccountId)};var category by rememberSaveable{mutableStateOf(initial?.categoryId)};var subcategory by rememberSaveable{mutableStateOf(initial?.subcategoryId)};var date by rememberSaveable{mutableStateOf(initial?.localDate ?: initial?.occurredAt?.let{Instant.ofEpochMilli(it).atZone(ZoneId.of(initial.zoneId)).toLocalDate().toString()}?:LocalDate.now().toString())};var description by rememberSaveable{mutableStateOf(initial?.description.orEmpty())};var mediaId by rememberSaveable{mutableStateOf(initial?.mediaId)};LaunchedEffect(accounts){if(account.isBlank())account=accounts.firstOrNull()?.id.orEmpty()};var quickDialog by remember{mutableStateOf(false)};var confirm by remember{mutableStateOf(false)};val parsed=parseMinor(amount);val eligible=categories.filter{it.kind.name==type.name};val eligibleSubs=subs.filter{it.categoryId==category};val valid=parsed!=null&&parsed>0&&account.isNotBlank()&&runCatching{LocalDate.parse(date)}.isSuccess&&if(type==TransactionType.TRANSFER)destination!=null&&destination!=account else category!=null
 LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{LazyRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){items(TransactionType.entries){value->FilterChip(type==value,{typeName=value.name;destination=null;category=null;subcategory=null},{Text(editorType(value))})}}};item{BudgetAmountField(amount,{amount=it},stringResource(R.string.transaction_editor_amount),stringResource(R.string.transactions_currency),isError=amount.isNotBlank()&&(parsed==null||parsed<=0))};item{EditorSelector(stringResource(R.string.transaction_editor_source),accounts,account,{it.id},{it.name}){account=it.id;if(destination==it.id)destination=null}};if(type==TransactionType.TRANSFER)item{EditorSelector(stringResource(R.string.transaction_editor_destination),accounts.filter{it.id!=account},destination,{it.id},{it.name}){destination=it.id}}else{item{EditorSelector(stringResource(R.string.transaction_editor_category),eligible,category,{it.id},{editorCategory(it)}){category=it.id;subcategory=null}};item{TextButton({quickDialog=true}){Text(stringResource(R.string.transaction_editor_new_category))}};if(eligibleSubs.isNotEmpty())item{EditorSelector(stringResource(R.string.transaction_editor_subcategory),eligibleSubs,subcategory,{it.id},{it.name}){subcategory=it.id}}};item{BudgetTextField(date,{date=it},stringResource(R.string.transaction_editor_date),supportingText=stringResource(R.string.transaction_editor_date_help))};item{com.budgetplusplus.core.ui.MediaPickerButton({uri,crop->importMedia(uri,crop){mediaId=it}});if(mediaId!=null)Text(stringResource(R.string.transaction_editor_image_selected),style=MaterialTheme.typography.bodySmall)};item{BudgetTextField(description,{if(it.length<=120)description=it},stringResource(R.string.transaction_editor_description),supportingText=stringResource(R.string.transaction_editor_description_count,description.length))};item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(cancel,Modifier.weight(1f)){Text(stringResource(R.string.transaction_editor_cancel))};Button({confirm=true},Modifier.weight(1f),enabled=valid){Text(stringResource(R.string.transaction_editor_save))}}}}
 if(quickDialog)QuickCategoryDialog(if(type==TransactionType.INCOME)CategoryKind.INCOME else CategoryKind.EXPENSE,{quickDialog=false}){name,kind,icon,color,sub->quick(name,kind,icon,color,sub){newCategory,newSub->category=newCategory;subcategory=newSub;quickDialog=false}}
 if(confirm)BudgetConfirmationDialog(stringResource(R.string.transaction_editor_confirm_title),stringResource(R.string.transaction_editor_confirm_message),{confirm=false;save(initial?.id,type,parsed!!,account,destination,category,subcategory,date,description,mediaId)},{confirm=false},confirmText=stringResource(R.string.transaction_editor_confirm))
}

@Composable
private fun QuickCategoryDialog(kind: CategoryKind, dismiss: () -> Unit, create: (String, CategoryKind, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var subcategory by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("category") }
    var color by remember { mutableStateOf("primary") }
    val icons = listOf("category", "home", "account")
    val colors = listOf("primary", "green", "orange", "blue")
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(stringResource(R.string.transaction_editor_quick_title)) },
        text = { LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { BudgetTextField(name, { name = it }, stringResource(R.string.transaction_editor_category_name)) }
            item { Text(stringResource(R.string.transaction_editor_icon)); LazyRow { items(icons) { value ->
                val description = quickIconLabel(value)
                FilterChip(icon == value, { icon = value }, { Icon(when(value) { "home" -> BudgetIcons.Home; "account" -> BudgetIcons.Account; else -> BudgetIcons.Category }, contentDescription = description) }, Modifier.padding(end = 6.dp))
            } } }
            item { Text(stringResource(R.string.transaction_editor_color)); LazyRow { items(colors) { value ->
                val description = quickColorLabel(value)
                FilterChip(color == value, { color = value }, { Box(Modifier.size(24.dp).background(quickColor(value), CircleShape).semantics { contentDescription = description }) }, Modifier.padding(end = 6.dp))
            } } }
            item { BudgetTextField(subcategory, { subcategory = it }, stringResource(R.string.transaction_editor_optional_subcategory)) }
        } },
        confirmButton = { TextButton({ create(name.trim(), kind, icon, color, subcategory.trim()) }, enabled = name.isNotBlank() && name.length <= 60) { Text(stringResource(R.string.transaction_editor_create_select)) } },
        dismissButton = { TextButton(dismiss) { Text(stringResource(R.string.transaction_editor_cancel)) } },
    )
}
@Composable private fun <T> EditorSelector(label:String,values:List<T>,selected:String?,id:(T)->String,text: @Composable (T) -> String,choose:(T)->Unit){var open by remember{mutableStateOf(false)};val current=values.firstOrNull{id(it)==selected};Box{OutlinedButton({open=true},Modifier.fillMaxWidth(),enabled=values.isNotEmpty()){Text(current?.let{text(it)}?:label)};DropdownMenu(open,{open=false}){values.forEach{value->DropdownMenuItem({Text(text(value))},{choose(value);open=false})}}}}
@Composable private fun editorType(value:TransactionType)=stringResource(when(value){TransactionType.EXPENSE->R.string.transaction_expense;TransactionType.INCOME->R.string.transaction_income;TransactionType.TRANSFER->R.string.transaction_transfer})
@Composable private fun editorCategory(value:Category)=value.customName?:stringResource(R.string.transaction_other)
@Composable private fun quickColorLabel(value:String)=stringResource(when(value){"green"->R.string.transaction_editor_color_green;"orange"->R.string.transaction_editor_color_orange;"blue"->R.string.transaction_editor_color_blue;else->R.string.transaction_editor_color_primary})
@Composable private fun quickIconLabel(value:String)=stringResource(when(value){"home"->R.string.transaction_editor_icon_home;"account"->R.string.transaction_editor_icon_account;else->R.string.transaction_editor_icon_category})
private fun quickColor(value:String)=when(value){"green"->Color(0xFF2E7D32);"orange"->Color(0xFFEF6C00);"blue"->Color(0xFF1565C0);else->Color(0xFF386A54)}
private fun formatMinor(value:Long)="${value/100}.${(value%100).toString().padStart(2,'0')}"
