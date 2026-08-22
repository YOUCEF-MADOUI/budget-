package com.budgetplusplus.feature.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.budgetplusplus.core.designsystem.financial.*
import com.budgetplusplus.core.model.*
import com.budgetplusplus.domain.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.*
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

enum class Period{WEEK,MONTH,YEAR,CUSTOM}
data class Interval(val from:LocalDate,val to:LocalDate)
sealed interface State{data object Loading:State;data object Error:State;data class Ready(val data:AnalyticsData,val interval:Interval):State}

@HiltViewModel class AnalyticsViewModel @Inject constructor(repository:AnalyticsRepository):ViewModel(){private val period=MutableStateFlow(Period.MONTH);private val custom=MutableStateFlow(default(Period.MONTH));private val retry=MutableStateFlow(0);val selected=period.asStateFlow()
 @OptIn(ExperimentalCoroutinesApi::class) val state=combine(period,custom,retry){p,c,_->if(p==Period.CUSTOM)c else default(p)}.flatMapLatest{i->val days=java.time.temporal.ChronoUnit.DAYS.between(i.from,i.to)+1;val prevTo=i.from.minusDays(1);val prevFrom=prevTo.minusDays(days-1);repository.observeAnalytics(i.from.toString(),i.to.toString(),prevFrom.toString(),prevTo.toString(),pMonthly(i)).map<AnalyticsData,State>{State.Ready(it,i)}.onStart{emit(State.Loading)}.catch{emit(State.Error)}}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),State.Loading)
 fun select(p:Period){period.value=p};fun custom(a:LocalDate,b:LocalDate){custom.value=Interval(minOf(a,b),maxOf(a,b));period.value=Period.CUSTOM};fun retry(){retry.value++}
 companion object{private fun default(p:Period,t:LocalDate=LocalDate.now())=when(p){Period.WEEK->Interval(t.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),t);Period.MONTH->Interval(t.withDayOfMonth(1),t);Period.YEAR->Interval(t.withDayOfYear(1),t);Period.CUSTOM->Interval(t.withDayOfMonth(1),t)};private fun pMonthly(i:Interval)=java.time.temporal.ChronoUnit.DAYS.between(i.from,i.to)>93}
}

@Composable fun AnalyticsScreen(onBack:(()->Unit)?,vm:AnalyticsViewModel=hiltViewModel()){val state by vm.state.collectAsStateWithLifecycle();val selected by vm.selected.collectAsStateWithLifecycle();var dates by remember{mutableStateOf(false)};Scaffold(topBar={BudgetTopAppBar(stringResource(R.string.analytics_title),onBackClick=onBack)}){p->when(val s=state){State.Loading->BudgetLoadingIndicator(Modifier.padding(p).fillMaxSize());State.Error->BudgetErrorState(stringResource(R.string.analytics_error_title),stringResource(R.string.analytics_error_message),vm::retry,Modifier.padding(p));is State.Ready->Content(s.data,s.interval,selected,{if(it==Period.CUSTOM)dates=true else vm.select(it)},Modifier.padding(p))}};if(dates)RangeDialog({dates=false}){a,b->vm.custom(a,b);dates=false}}

@Composable private fun Content(data:AnalyticsData,interval:Interval,selected:Period,choose:(Period)->Unit,modifier:Modifier){val empty=data.current.incomeMinor==0L&&data.current.expenseMinor==0L;LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){item{LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){items(Period.entries){v->FilterChip(v==selected,{choose(v)},{Text(period(v))})}}};item{Text(stringResource(R.string.analytics_interval,interval.from.toString(),interval.to.toString()),style=MaterialTheme.typography.labelLarge)};if(empty){item{BudgetEmptyState(stringResource(R.string.analytics_empty),stringResource(R.string.analytics_empty_message),Modifier.fillParentMaxHeight())}}else{
 item{IncomeExpenseCard(stringResource(R.string.analytics_current_period),stringResource(R.string.analytics_income),stringResource(R.string.analytics_expenses),stringResource(R.string.analytics_net),data.current.incomeMinor,data.current.expenseMinor,data.currencyCode,Modifier.fillMaxWidth())}
 item{Comparison(data)};item{Text(stringResource(R.string.analytics_evolution),style=MaterialTheme.typography.titleLarge)};item{EvolutionChart(data.evolution,Modifier.fillMaxWidth().height(220.dp))};item{Averages(data)};item{Text(stringResource(R.string.analytics_categories),style=MaterialTheme.typography.titleLarge)};item{CategoryChart(data.categories,data.current.expenseMinor,data.currencyCode)}}}}

@Composable private fun Comparison(d:AnalyticsData){BudgetCard(Modifier.fillMaxWidth()){Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text(stringResource(R.string.analytics_comparison),style=MaterialTheme.typography.titleMedium);MetricDelta(stringResource(R.string.analytics_income),Math.subtractExact(d.current.incomeMinor,d.previous.incomeMinor),d.previous.incomeMinor,d.currencyCode);MetricDelta(stringResource(R.string.analytics_expenses),Math.subtractExact(d.current.expenseMinor,d.previous.expenseMinor),d.previous.expenseMinor,d.currencyCode);MetricDelta(stringResource(R.string.analytics_net),Math.subtractExact(d.current.netMinor,d.previous.netMinor),d.previous.netMinor,d.currencyCode)}}}
@Composable private fun MetricDelta(label:String,delta:Long,previous:Long,currency:String){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label);Column{MoneyText(delta,currency,tone=if(delta>=0)AmountTone.Income else AmountTone.Expense);Text(stringResource(R.string.analytics_percent,percent(delta,previous)),style=MaterialTheme.typography.labelSmall)}}}
private fun percent(delta:Long,previous:Long):Int=if(previous==0L)0 else BigDecimal.valueOf(delta).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(kotlin.math.abs(previous)),0,RoundingMode.HALF_UP).coerceIn(BigDecimal.valueOf(Int.MIN_VALUE.toLong()),BigDecimal.valueOf(Int.MAX_VALUE.toLong())).toInt()
@Composable private fun Averages(d:AnalyticsData){BudgetCard(Modifier.fillMaxWidth()){Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text(stringResource(R.string.analytics_averages),style=MaterialTheme.typography.titleMedium);Text(stringResource(R.string.analytics_daily_income));MoneyText(d.dailyAverageIncomeMinor,d.currencyCode,tone=AmountTone.Income);Text(stringResource(R.string.analytics_daily));MoneyText(d.dailyAverageExpenseMinor,d.currencyCode,tone=AmountTone.Expense);Text(stringResource(R.string.analytics_monthly_income));MoneyText(d.monthlyAverageIncomeMinor,d.currencyCode,tone=AmountTone.Income);Text(stringResource(R.string.analytics_monthly));MoneyText(d.monthlyAverageExpenseMinor,d.currencyCode,tone=AmountTone.Expense)}}}

@Composable private fun EvolutionChart(points:List<AnalyticsPoint>,modifier:Modifier){val income=Color(0xFF2E7D32);val expense=MaterialTheme.colorScheme.error;val net=MaterialTheme.colorScheme.primary;val desc=stringResource(R.string.analytics_chart_description,points.size);val maximum=(points.maxOfOrNull{maxOf(it.incomeMinor,it.expenseMinor,kotlin.math.abs(it.netMinor))}?:1).coerceAtLeast(1);Canvas(modifier.semantics{contentDescription=desc}){val step=size.width/maxOf(1,points.size);points.forEachIndexed{i,v->val x=step*(i+.5f);fun y(n:Long)=size.height-(kotlin.math.abs(n).toFloat()/maximum.toFloat())*size.height;drawLine(income,Offset(x-4.dp.toPx(),size.height),Offset(x-4.dp.toPx(),y(v.incomeMinor)),4.dp.toPx());drawLine(expense,Offset(x+4.dp.toPx(),size.height),Offset(x+4.dp.toPx(),y(v.expenseMinor)),4.dp.toPx());drawCircle(net,3.dp.toPx(),Offset(x,y(v.netMinor)))}}}
@Composable private fun CategoryChart(values:List<AnalyticsCategory>,total:Long,currency:String){if(values.isEmpty()){Text(stringResource(R.string.analytics_no_categories));return};BudgetCard(Modifier.fillMaxWidth()){Column(verticalArrangement=Arrangement.spacedBy(12.dp)){values.take(8).forEachIndexed{i,v->val progress=if(total<=0)0f else (v.amountMinor.toFloat()/total.toFloat()).coerceIn(0f,1f);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(stringResource(R.string.analytics_rank,i+1,cat(v)));MoneyText(v.amountMinor,currency)};LinearProgressIndicator({progress},Modifier.fillMaxWidth())}}}}
@Composable private fun cat(v:AnalyticsCategory)=v.customName?:v.nameKey?.let{systemCategory(it)}?:stringResource(R.string.analytics_system_category)
@Composable private fun systemCategory(key:String)=stringResource(when(key){"category_food"->R.string.analytics_food;"category_transport"->R.string.analytics_transport;"category_housing"->R.string.analytics_housing;"category_health"->R.string.analytics_health;"category_leisure"->R.string.analytics_leisure;"category_utilities"->R.string.analytics_utilities;"category_education"->R.string.analytics_education;"category_family"->R.string.analytics_family;"category_clothing"->R.string.analytics_clothing;"category_taxes"->R.string.analytics_taxes;else->R.string.analytics_system_category})
@Composable private fun period(v:Period)=stringResource(when(v){Period.WEEK->R.string.analytics_week;Period.MONTH->R.string.analytics_month;Period.YEAR->R.string.analytics_year;Period.CUSTOM->R.string.analytics_custom})
@OptIn(ExperimentalMaterial3Api::class) @Composable private fun RangeDialog(dismiss:()->Unit,done:(LocalDate,LocalDate)->Unit){val s=rememberDateRangePickerState();DatePickerDialog(onDismissRequest=dismiss,confirmButton={TextButton({done(Instant.ofEpochMilli(s.selectedStartDateMillis!!).atZone(ZoneOffset.UTC).toLocalDate(),Instant.ofEpochMilli(s.selectedEndDateMillis!!).atZone(ZoneOffset.UTC).toLocalDate())},enabled=s.selectedStartDateMillis!=null&&s.selectedEndDateMillis!=null){Text(stringResource(R.string.analytics_apply))}},dismissButton={TextButton(dismiss){Text(stringResource(R.string.analytics_cancel))}}){DateRangePicker(s,modifier=Modifier.height(500.dp))}}
