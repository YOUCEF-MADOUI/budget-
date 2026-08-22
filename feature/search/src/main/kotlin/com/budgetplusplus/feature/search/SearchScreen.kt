package com.budgetplusplus.feature.search

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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.budgetplusplus.core.designsystem.components.*
import com.budgetplusplus.core.designsystem.financial.*
import com.budgetplusplus.core.designsystem.icons.BudgetIcons
import com.budgetplusplus.core.model.*
import com.budgetplusplus.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ResultState(val items:List<FinanceTransaction> = emptyList(),val cursor:SearchCursor?=null,val loading:Boolean=false,val end:Boolean=false,val error:Boolean=false)

@HiltViewModel class SearchViewModel @Inject constructor(private val repository:SearchRepository,accountsRepository:AccountRepository,categoryRepository:CategoryRepository,private val saved:SavedStateHandle):ViewModel(){
 private val _filter=MutableStateFlow(restore());val filter=_filter.asStateFlow();private val _state=MutableStateFlow(ResultState());val state=_state.asStateFlow();val accounts=accountsRepository.observeAccounts().map{it.filterNot(Account::isArchived)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList());val categories=categoryRepository.observeCategories().map{it.filterNot(Category::isArchived)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList());val subs=categoryRepository.observeSubcategories().map{it.filterNot(Subcategory::isArchived)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 init{apply()}
 fun update(value:TransactionSearchFilter){_filter.value=value;persist(value)}
 fun reset(){update(TransactionSearchFilter());apply()}
 fun apply(){_state.value=ResultState();loadMore()}
 fun loadMore(){val current=_state.value;if(current.loading||current.end)return;_state.value=current.copy(loading=true,error=false);viewModelScope.launch{runCatching{repository.search(_filter.value,current.cursor)}.onSuccess{page->val window=(current.items+page.items).takeLast(1000);_state.value=ResultState(window,page.nextCursor,false,page.nextCursor==null,false)}.onFailure{_state.value=current.copy(loading=false,error=true)}}}
 private fun persist(v:TransactionSearchFilter){saved["q"]=v.text;saved["type"]=v.type?.name;saved["account"]=v.accountId;saved["category"]=v.categoryId;saved["sub"]=v.subcategoryId;saved["from"]=v.fromDate;saved["to"]=v.toDate;saved["min"]=v.minimumMinor;saved["max"]=v.maximumMinor;saved["sort"]=v.sort.name}
 private fun restore()=TransactionSearchFilter(saved["q"]?:"",(saved.get<String>("type"))?.let(TransactionType::valueOf),saved["account"],saved["category"],saved["sub"],saved["from"],saved["to"],saved["min"],saved["max"],saved.get<String>("sort")?.let(TransactionSort::valueOf)?:TransactionSort.DATE_DESC)
}

@Composable fun SearchScreen(onBack:()->Unit,vm:SearchViewModel=hiltViewModel()){val f by vm.filter.collectAsStateWithLifecycle();val s by vm.state.collectAsStateWithLifecycle();val accounts by vm.accounts.collectAsStateWithLifecycle();val categories by vm.categories.collectAsStateWithLifecycle();val subs by vm.subs.collectAsStateWithLifecycle();var filters by remember{mutableStateOf(false)}
 Scaffold(topBar={BudgetTopAppBar(stringResource(R.string.search_title),onBackClick=onBack)}){p->Column(Modifier.padding(p).fillMaxSize()){BudgetSearchBar(f.text,{vm.update(f.copy(text=it))},Modifier.padding(horizontal=16.dp),stringResource(R.string.search_hint));Row(Modifier.padding(horizontal=16.dp).fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){TextButton({filters=!filters}){Text(stringResource(R.string.search_filters))};TextButton(vm::reset){Text(stringResource(R.string.search_reset))};Button(vm::apply){Text(stringResource(R.string.search_apply))}}
  if(filters)FilterPanel(f,vm::update,accounts,categories,subs)
  when{ s.error->BudgetErrorState(stringResource(R.string.search_error_title),stringResource(R.string.search_error_message),vm::loadMore,Modifier.fillMaxSize());s.items.isEmpty()&&s.loading->BudgetLoadingIndicator(Modifier.fillMaxSize());s.items.isEmpty()->BudgetEmptyState(stringResource(R.string.search_empty),stringResource(R.string.search_empty_message),Modifier.fillMaxSize());else->LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(12.dp)){items(s.items,key={it.id}){t->ResultRow(t)};if(!s.end)item{LaunchedEffect(s.items.size){vm.loadMore()};Box(Modifier.fillMaxWidth().padding(16.dp)){CircularProgressIndicator()}}}}
 }}}

@Composable private fun FilterPanel(f:TransactionSearchFilter,update:(TransactionSearchFilter)->Unit,accounts:List<Account>,categories:List<Category>,subs:List<Subcategory>){var minText by remember(f.minimumMinor){mutableStateOf(f.minimumMinor?.let(::format).orEmpty())};var maxText by remember(f.maximumMinor){mutableStateOf(f.maximumMinor?.let(::format).orEmpty())};ElevatedCard(Modifier.padding(12.dp).fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
 Text(stringResource(R.string.search_type));LazyRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){item{FilterChip(f.type==null,{update(f.copy(type=null))},{Text(stringResource(R.string.search_all))})};items(TransactionType.entries){v->FilterChip(f.type==v,{update(f.copy(type=v))},{Text(typeName(v))})}}
 Select(stringResource(R.string.search_account),accounts,f.accountId,{it.id},{it.name}){update(f.copy(accountId=it?.id))};Select(stringResource(R.string.search_category),categories,f.categoryId,{it.id},{catName(it)}){v->update(f.copy(categoryId=v?.id,subcategoryId=null))};Select(stringResource(R.string.search_subcategory),subs.filter{it.categoryId==f.categoryId},f.subcategoryId,{it.id},{it.name}){update(f.copy(subcategoryId=it?.id))}
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){BudgetTextField(f.fromDate.orEmpty(),{update(f.copy(fromDate=it.ifBlank{null}))},stringResource(R.string.search_from),Modifier.weight(1f));BudgetTextField(f.toDate.orEmpty(),{update(f.copy(toDate=it.ifBlank{null}))},stringResource(R.string.search_to),Modifier.weight(1f))}
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){com.budgetplusplus.core.designsystem.components.BudgetAmountField(minText,{minText=it;update(f.copy(minimumMinor=parse(it)))},stringResource(R.string.search_min),stringResource(R.string.search_currency),Modifier.weight(1f));com.budgetplusplus.core.designsystem.components.BudgetAmountField(maxText,{maxText=it;update(f.copy(maximumMinor=parse(it)))},stringResource(R.string.search_max),stringResource(R.string.search_currency),Modifier.weight(1f))}
 Text(stringResource(R.string.search_sort));LazyRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){items(TransactionSort.entries){v->FilterChip(f.sort==v,{update(f.copy(sort=v))},{Text(sortName(v))})}}
 }}}
@Composable private fun <T> Select(label:String,values:List<T>,selected:String?,id:(T)->String,text:@Composable (T)->String,choose:(T?)->Unit){var open by remember{mutableStateOf(false)};val current=values.firstOrNull{id(it)==selected};Box{OutlinedButton({open=true},Modifier.fillMaxWidth()){Text(current?.let{text(it)}?:label)};DropdownMenu(open,{open=false}){DropdownMenuItem({Text(stringResource(R.string.search_all))},{choose(null);open=false});values.forEach{v->DropdownMenuItem({Text(text(v))},{choose(v);open=false})}}}}
@Composable private fun ResultRow(t:FinanceTransaction){val account=t.destinationAccountName?.let{stringResource(R.string.search_transfer_accounts,t.accountName,it)}?:t.accountName;TransactionRow(t.categoryCustomName?:t.subcategoryName?:typeName(t.type),t.description,DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(t.occurredAt)),account,t.amountMinor,t.currencyCode,when(t.type){TransactionType.INCOME->TransactionVisualType.Income;TransactionType.EXPENSE->TransactionVisualType.Expense;TransactionType.TRANSFER->TransactionVisualType.Transfer},BudgetIcons.Category)}
@Composable private fun typeName(v:TransactionType)=stringResource(when(v){TransactionType.EXPENSE->R.string.search_expense;TransactionType.INCOME->R.string.search_income;TransactionType.TRANSFER->R.string.search_transfer})
@Composable private fun sortName(v:TransactionSort)=stringResource(when(v){TransactionSort.DATE_DESC->R.string.search_date_desc;TransactionSort.DATE_ASC->R.string.search_date_asc;TransactionSort.AMOUNT_DESC->R.string.search_amount_desc;TransactionSort.AMOUNT_ASC->R.string.search_amount_asc})
@Composable private fun catName(v:Category)=v.customName?:stringResource(R.string.search_system_category)
private fun format(v:Long)="${v/100}.${(v%100).toString().padStart(2,'0')}";internal fun parse(v:String):Long?{if(v.isBlank())return null;val p=v.replace(',','.').split('.');if(p.size>2||p.any{x->x.any{!it.isDigit()}})return null;val a=p[0].ifBlank{"0"}.toLongOrNull()?:return null;val b=p.getOrNull(1).orEmpty();if(b.length>2)return null;return runCatching{Math.addExact(Math.multiplyExact(a,100),b.padEnd(2,'0').ifBlank{"0"}.toLong())}.getOrNull()}
