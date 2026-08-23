package com.budgetplusplus.feature.budgets

import androidx.compose.foundation.clickable
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
import com.budgetplusplus.core.designsystem.financial.FinancialBudgetCard
import com.budgetplusplus.core.model.*
import com.budgetplusplus.domain.repository.BudgetRepository
import com.budgetplusplus.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class BudgetsViewModel @Inject constructor(budgetRepository: BudgetRepository, categoryRepository: CategoryRepository) : ViewModel() {
    private val repository = budgetRepository
    val budgets = repository.observeBudgets().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories = categoryRepository.observeCategories(CategoryKind.EXPENSE).map { it.filterNot(Category::isArchived) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _error = MutableStateFlow(false); val error = _error.asStateFlow()
    fun save(input: BudgetInput, done: () -> Unit) = viewModelScope.launch { runCatching { repository.save(input) }.onSuccess { _error.value=false;done() }.onFailure { _error.value=true } }
    fun archive(value: BudgetProgress) = viewModelScope.launch { runCatching { repository.setArchived(value.id,!value.isArchived) }.onFailure { _error.value=true } }
    fun clearError(){_error.value=false}
}

@Composable
fun BudgetsScreen(onBack:(()->Unit)?,viewModel:BudgetsViewModel=hiltViewModel()){
    val budgets by viewModel.budgets.collectAsStateWithLifecycle();val categories by viewModel.categories.collectAsStateWithLifecycle();val error by viewModel.error.collectAsStateWithLifecycle()
    var history by remember{mutableStateOf(false)};var editor by remember{mutableStateOf<BudgetProgress?>(null)};var create by remember{mutableStateOf(false)};var pending by remember{mutableStateOf<BudgetProgress?>(null)}
    val snackbar=remember{SnackbarHostState()};val errorText=stringResource(R.string.budgets_error);LaunchedEffect(error){if(error){snackbar.showSnackbar(errorText);viewModel.clearError()}}
    val visible=budgets.filter{it.isArchived==history}
    Scaffold(snackbarHost={SnackbarHost(snackbar)},topBar={BudgetTopAppBar(stringResource(R.string.budgets_title),onBackClick=onBack)},floatingActionButton={if(!history)FloatingActionButton({create=true}){Text(stringResource(R.string.budgets_add_symbol))}}){padding->Column(Modifier.padding(padding).fillMaxSize()){
        Row(Modifier.padding(horizontal=16.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(!history,{history=false},{Text(stringResource(R.string.budgets_active))});FilterChip(history,{history=true},{Text(stringResource(R.string.budgets_history))})}
        if(visible.isEmpty())BudgetEmptyState(stringResource(if(history)R.string.budgets_no_history else R.string.budgets_empty),stringResource(if(history)R.string.budgets_no_history_message else R.string.budgets_empty_message),Modifier.fillMaxSize(),if(history)null else stringResource(R.string.budgets_add_action),if(history)null else({create=true}))
        else LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){items(visible,key={it.id}){budget->Column{
            FinancialBudgetCard(budget.name,stringResource(R.string.budgets_planned),stringResource(R.string.budgets_spent),stringResource(R.string.budgets_remaining),budget.amountMinor,budget.spentMinor,budget.currencyCode,Modifier.fillMaxWidth().clickable{editor=budget},budget.warningThresholdPercent)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){TextButton({editor=budget}){Text(stringResource(R.string.budgets_edit))};TextButton({pending=budget}){Text(stringResource(if(budget.isArchived)R.string.budgets_restore else R.string.budgets_archive))}}
        }}}
    }}
    if(create)BudgetEditor(null,categories,{create=false}){viewModel.save(it){create=false}}
    editor?.let{value->BudgetEditor(value,categories,{editor=null}){viewModel.save(it){editor=null}}}
    pending?.let{value->BudgetConfirmationDialog(stringResource(if(value.isArchived)R.string.budgets_restore_title else R.string.budgets_archive_title),stringResource(if(value.isArchived)R.string.budgets_restore_message else R.string.budgets_archive_message,value.name),{viewModel.archive(value);pending=null},{pending=null},confirmText=stringResource(if(value.isArchived)R.string.budgets_restore else R.string.budgets_archive),destructive=!value.isArchived)}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun BudgetEditor(value:BudgetProgress?,categories:List<Category>,dismiss:()->Unit,save:(BudgetInput)->Unit){
    val today=LocalDate.now();var name by remember{mutableStateOf(value?.name.orEmpty())};var amount by remember{mutableStateOf(value?.amountMinor?.let{formatInput(it)}.orEmpty())};var scope by remember{mutableStateOf(value?.scope?:BudgetScope.GLOBAL)};var category by remember{mutableStateOf(value?.categoryId)};var period by remember{mutableStateOf(value?.periodType?:BudgetPeriodType.MONTHLY)};var start by remember{mutableStateOf(value?.startDate?.let(LocalDate::parse)?:today.withDayOfMonth(1))};var end by remember{mutableStateOf(value?.endDate?.let(LocalDate::parse)?:today.withDayOfMonth(today.lengthOfMonth()))};var threshold by remember{mutableStateOf((value?.warningThresholdPercent?:90).toString())};var showDates by remember{mutableStateOf(false)}
    val parsed=parseMinor(amount);val thresholdValue=threshold.toIntOrNull();val valid=name.isNotBlank()&&parsed!=null&&parsed>0&&thresholdValue != null&&thresholdValue in 50..99&&(scope==BudgetScope.GLOBAL||category!=null)
    AlertDialog(onDismissRequest=dismiss,title={Text(stringResource(if(value==null)R.string.budgets_new else R.string.budgets_edit_title))},text={LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{BudgetTextField(name,{if(it.length<=60)name=it},stringResource(R.string.budgets_name))};item{com.budgetplusplus.core.designsystem.components.BudgetAmountField(amount,{amount=it},stringResource(R.string.budgets_amount),stringResource(R.string.budgets_currency),isError=amount.isNotEmpty()&&(parsed==null||parsed<=0))}
        item{Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){BudgetScope.entries.forEach{s->FilterChip(scope==s,{scope=s;if(s==BudgetScope.GLOBAL)category=null},{Text(stringResource(if(s==BudgetScope.GLOBAL)R.string.budgets_global else R.string.budgets_category))})}}}
        if(scope==BudgetScope.CATEGORY)item{Selector(stringResource(R.string.budgets_choose_category),categories,category,{categoryName(it)}){category=it.id}}
        item{Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){BudgetPeriodType.entries.forEach{p->FilterChip(period==p,{period=p;if(p==BudgetPeriodType.MONTHLY){start=today.withDayOfMonth(1);end=today.withDayOfMonth(today.lengthOfMonth())}},{Text(stringResource(if(p==BudgetPeriodType.MONTHLY)R.string.budgets_monthly else R.string.budgets_custom))})}}}
        item{OutlinedButton({showDates=true},Modifier.fillMaxWidth(),enabled=period==BudgetPeriodType.CUSTOM){Text(stringResource(R.string.budgets_dates,start.toString(),end.toString()))}}
        item{BudgetTextField(threshold,{threshold=it.filter(Char::isDigit)},stringResource(R.string.budgets_threshold),supportingText=if(thresholdValue==null||thresholdValue !in 50..99)stringResource(R.string.budgets_threshold_help)else null,isError=thresholdValue==null||thresholdValue !in 50..99)}
    }},confirmButton={TextButton({save(BudgetInput(value?.id,name.trim(),scope,category,parsed!!,"DZD",period,start.toString(),end.toString(),thresholdValue!!))},enabled=valid){Text(stringResource(R.string.budgets_save))}},dismissButton={TextButton(dismiss){Text(stringResource(R.string.budgets_cancel))}})
    if(showDates){val state=rememberDateRangePickerState();DatePickerDialog(onDismissRequest={showDates=false},confirmButton={TextButton({start=Instant.ofEpochMilli(state.selectedStartDateMillis!!).atZone(ZoneOffset.UTC).toLocalDate();end=Instant.ofEpochMilli(state.selectedEndDateMillis!!).atZone(ZoneOffset.UTC).toLocalDate();showDates=false},enabled=state.selectedStartDateMillis!=null&&state.selectedEndDateMillis!=null){Text(stringResource(R.string.budgets_apply))}},dismissButton={TextButton({showDates=false}){Text(stringResource(R.string.budgets_cancel))}}){DateRangePicker(state,modifier=Modifier.height(500.dp))}}
}
@Composable private fun Selector(label: String, values: List<Category>, selected: String?, text: @Composable (Category) -> String, choose: (Category) -> Unit){var open by remember{mutableStateOf(false)};val value=values.firstOrNull{it.id==selected};Box{OutlinedButton({open=true},Modifier.fillMaxWidth()){Text(value?.let{text(it)}?:label)};DropdownMenu(open,{open=false}){values.forEach{v->DropdownMenuItem({Text(text(v))},{choose(v);open=false})}}}}
@Composable private fun categoryName(value:Category)=value.customName?:value.nameKey?.let{systemName(it)}?:stringResource(R.string.budgets_other)
@Composable private fun systemName(key:String)=stringResource(when(key){"category_food"->R.string.budgets_food;"category_transport"->R.string.budgets_transport;"category_housing"->R.string.budgets_housing;"category_health"->R.string.budgets_health;"category_leisure"->R.string.budgets_leisure;"category_utilities"->R.string.budgets_utilities;"category_education"->R.string.budgets_education;"category_family"->R.string.budgets_family;"category_clothing"->R.string.budgets_clothing;"category_taxes"->R.string.budgets_taxes;else->R.string.budgets_other})
private fun formatInput(minor:Long)="${minor/100}.${(minor%100).toString().padStart(2,'0')}"
internal fun parseMinor(value:String):Long?{val p=value.trim().replace(',','.').split('.');if(p.size>2||p.any{x->x.any{!it.isDigit()}})return null;val major=p[0].ifEmpty{"0"}.toLongOrNull()?:return null;val minor=p.getOrNull(1).orEmpty();if(minor.length>2)return null;return runCatching{Math.addExact(Math.multiplyExact(major,100),minor.padEnd(2,'0').ifEmpty{"0"}.toLong())}.getOrNull()}
