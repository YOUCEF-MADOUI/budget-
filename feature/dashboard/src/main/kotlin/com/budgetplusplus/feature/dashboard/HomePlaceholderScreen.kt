package com.budgetplusplus.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetplusplus.core.designsystem.components.BudgetCard
import com.budgetplusplus.core.designsystem.components.BudgetEmptyState
import com.budgetplusplus.core.designsystem.components.BudgetErrorState
import com.budgetplusplus.core.designsystem.components.BudgetLoadingIndicator
import com.budgetplusplus.core.designsystem.components.BudgetTopAppBar
import com.budgetplusplus.core.designsystem.financial.*
import com.budgetplusplus.core.designsystem.icons.BudgetIcons
import com.budgetplusplus.core.model.*
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date
import kotlin.math.max

@Composable
fun HomePlaceholderScreen(
    onDesignSystemClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    var showRangePicker by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { Box { BudgetTopAppBar(stringResource(R.string.dashboard_title), onMoreClick = { showMenu = true }); DropdownMenu(showMenu, { showMenu = false }) {
            DropdownMenuItem({ Text(stringResource(R.string.home_categories)) }, { showMenu = false; onCategoriesClick() })
            DropdownMenuItem({ Text(stringResource(R.string.technical_open_design_system)) }, { showMenu = false; onDesignSystemClick() })
        } } },
        modifier = modifier,
    ) { padding ->
        when (val value = state) {
            DashboardUiState.Loading -> Box(Modifier.padding(padding).fillMaxSize()) { BudgetLoadingIndicator(Modifier.fillMaxSize()) }
            DashboardUiState.Error -> BudgetErrorState(stringResource(R.string.dashboard_error_title), stringResource(R.string.dashboard_error_message), viewModel::retry, Modifier.padding(padding))
            is DashboardUiState.Content -> DashboardContent(
                data = value.data,
                interval = value.interval,
                selectedPeriod = selectedPeriod,
                onPeriod = { period -> if (period == DashboardPeriod.CUSTOM) showRangePicker = true else viewModel.selectPeriod(period) },
                onAccountsClick = onAccountsClick,
                onTransactionsClick = onTransactionsClick,
                modifier = Modifier.padding(padding),
            )
        }
    }
    if (showRangePicker) CustomRangeDialog(
        onDismiss = { showRangePicker = false },
        onConfirm = { from, to -> viewModel.selectCustom(from, to); showRangePicker = false },
    )
}

@Composable
private fun DashboardContent(
    data: DashboardData,
    interval: DateInterval,
    selectedPeriod: DashboardPeriod,
    onPeriod: (DashboardPeriod) -> Unit,
    onAccountsClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (data.accounts.isEmpty()) {
        BudgetEmptyState(
            title = stringResource(R.string.dashboard_no_account_title),
            message = stringResource(R.string.dashboard_no_account_message),
            actionText = stringResource(R.string.dashboard_add_account),
            onAction = onAccountsClick,
            modifier = modifier.fillMaxSize(),
        )
        return
    }
    val periodLabel = intervalLabel(interval)
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { BalanceCard(stringResource(R.string.dashboard_total_balance), data.totalBalanceMinor, data.currencyCode, Modifier.fillMaxWidth()) }
        item { PeriodFilters(selectedPeriod, onPeriod) }
        item { Text(periodLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { IncomeExpenseCard(periodLabel, stringResource(R.string.dashboard_income), stringResource(R.string.dashboard_expenses), stringResource(R.string.dashboard_net), data.incomeMinor, data.expenseMinor, data.currencyCode, Modifier.fillMaxWidth()) }
        item { SectionTitle(stringResource(R.string.dashboard_evolution)) }
        item {
            if (data.evolution.size <= 1) DashboardSectionEmpty(stringResource(R.string.dashboard_no_evolution))
            else BalanceEvolutionChart(data.evolution, Modifier.fillMaxWidth().height(180.dp))
        }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { SectionTitle(stringResource(R.string.dashboard_accounts)); TextButton(onClick = onAccountsClick) { Text(stringResource(R.string.dashboard_see_all)) } } }
        item { LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(data.accounts, key = { it.id }) { account -> AccountCard(account.name, accountTypeLabel(account.type), account.currentBalanceMinor, account.currencyCode, BudgetIcons.Account, onAccountsClick, Modifier.width(280.dp)) } } }
        item { SectionTitle(stringResource(R.string.dashboard_categories)) }
        item { CategoryChart(data.categoryTotals, data.expenseMinor, data.currencyCode, onTransactionsClick) }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { SectionTitle(stringResource(R.string.dashboard_recent)); TextButton(onClick = onTransactionsClick) { Text(stringResource(R.string.dashboard_see_all)) } } }
        if (data.recentTransactions.isEmpty()) item { DashboardSectionEmpty(stringResource(R.string.dashboard_no_recent), stringResource(R.string.dashboard_add_transaction), onTransactionsClick) }
        else items(data.recentTransactions, key = { it.id }) { transaction -> RecentTransaction(transaction, onTransactionsClick) }
    }
}

@Composable
private fun PeriodFilters(selected: DashboardPeriod, onPeriod: (DashboardPeriod) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(DashboardPeriod.entries) { period -> FilterChip(selected == period, { onPeriod(period) }, { Text(periodLabel(period)) }) }
    }
}

@Composable private fun periodLabel(period: DashboardPeriod) = stringResource(when(period) { DashboardPeriod.WEEK -> R.string.dashboard_week; DashboardPeriod.MONTH -> R.string.dashboard_month; DashboardPeriod.YEAR -> R.string.dashboard_year; DashboardPeriod.CUSTOM -> R.string.dashboard_custom })

@Composable
private fun BalanceEvolutionChart(points: List<BalancePoint>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val min = points.minOf { it.balanceMinor }
    val maxBalance = points.maxOf { it.balanceMinor }
    val range = max(1L, maxBalance - min)
    val description = stringResource(R.string.dashboard_chart_description, points.size)
    Canvas(modifier.semantics { contentDescription = description }) {
        repeat(4) { index -> val y = size.height * index / 3f; drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx()) }
        val denominator = max(1, points.lastIndex)
        points.zipWithNext().forEachIndexed { index, pair ->
            fun y(value: Long): Float = size.height - ((value - min).toFloat() / range.toFloat()) * size.height
            val x1 = size.width * index / denominator
            val x2 = size.width * (index + 1) / denominator
            drawLine(lineColor, Offset(x1, y(pair.first.balanceMinor)), Offset(x2, y(pair.second.balanceMinor)), 3.dp.toPx(), StrokeCap.Round)
        }
    }
}

@Composable
private fun CategoryChart(categories: List<CategoryTotal>, totalExpense: Long, currency: String, onClick: () -> Unit) {
    if (categories.isEmpty()) { DashboardSectionEmpty(stringResource(R.string.dashboard_no_categories)); return }
    BudgetCard(Modifier.fillMaxWidth()) { Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        categories.take(6).forEach { category ->
            val progress = if (totalExpense <= 0) 0f else (category.amountMinor.toFloat() / totalExpense.toFloat()).coerceIn(0f, 1f)
            Column { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(categoryName(category), style = MaterialTheme.typography.bodyMedium); MoneyText(category.amountMinor, currency) }; LinearProgressIndicator({ progress }, Modifier.fillMaxWidth()); Text(stringResource(R.string.dashboard_percentage, (progress * 100).toInt()), style = MaterialTheme.typography.labelSmall) }
        }
        TextButton(onClick = onClick) { Text(stringResource(R.string.dashboard_view_operations)) }
    } }
}

@Composable
private fun RecentTransaction(transaction: FinanceTransaction, onClick: () -> Unit) {
    val destination = transaction.destinationAccountName
    val account = destination?.let { stringResource(R.string.dashboard_transfer_accounts, transaction.accountName, it) } ?: transaction.accountName
    TransactionRow(
        category = transaction.categoryCustomName ?: transaction.categoryNameKey?.let { systemCategoryName(it) } ?: transactionTypeLabel(transaction.type),
        description = transaction.description,
        date = DateFormat.getDateInstance(DateFormat.SHORT).format(Date(transaction.occurredAt)),
        account = account,
        amountMinor = transaction.amountMinor,
        currencyCode = transaction.currencyCode,
        type = when(transaction.type) { TransactionType.INCOME -> TransactionVisualType.Income; TransactionType.EXPENSE -> TransactionVisualType.Expense; TransactionType.TRANSFER -> TransactionVisualType.Transfer },
        categoryIcon = BudgetIcons.Category,
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRangeDialog(onDismiss: () -> Unit, onConfirm: (LocalDate, LocalDate) -> Unit) {
    val state = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null, onClick = {
            val from = Instant.ofEpochMilli(state.selectedStartDateMillis!!).atZone(ZoneOffset.UTC).toLocalDate()
            val to = Instant.ofEpochMilli(state.selectedEndDateMillis!!).atZone(ZoneOffset.UTC).toLocalDate()
            onConfirm(from, to)
        }) { Text(stringResource(R.string.dashboard_apply)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dashboard_cancel)) } },
    ) { DateRangePicker(state = state, title = { Text(stringResource(R.string.dashboard_choose_period), Modifier.padding(16.dp)) }, modifier = Modifier.height(500.dp)) }
}

@Composable private fun DashboardSectionEmpty(message: String, action: String? = null, onAction: (() -> Unit)? = null) { BudgetCard(Modifier.fillMaxWidth()) { Column { Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant); if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action) } } } }
@Composable private fun SectionTitle(text: String) { Text(text, style = MaterialTheme.typography.titleLarge) }
@Composable private fun intervalLabel(interval: DateInterval): String { val f = DateFormat.getDateInstance(DateFormat.MEDIUM); return stringResource(R.string.dashboard_period_value, f.format(Date.from(interval.from.atStartOfDay(ZoneId.systemDefault()).toInstant())), f.format(Date.from(interval.toInclusive.atStartOfDay(ZoneId.systemDefault()).toInstant()))) }
@Composable private fun accountTypeLabel(type: AccountType) = stringResource(when(type) { AccountType.CASH -> R.string.dashboard_account_cash; AccountType.BANK -> R.string.dashboard_account_bank; AccountType.SAVINGS -> R.string.dashboard_account_savings; AccountType.CARD -> R.string.dashboard_account_card; AccountType.OTHER -> R.string.dashboard_other })
@Composable private fun categoryName(value: CategoryTotal) = value.customName ?: value.nameKey?.let { systemCategoryName(it) } ?: stringResource(R.string.dashboard_other)
@Composable private fun systemCategoryName(key: String) = stringResource(when(key) { "category_food" -> R.string.dashboard_food; "category_transport" -> R.string.dashboard_transport; "category_housing" -> R.string.dashboard_housing; "category_health" -> R.string.dashboard_health; "category_leisure" -> R.string.dashboard_leisure; "category_utilities" -> R.string.dashboard_utilities; "category_education" -> R.string.dashboard_education; "category_family" -> R.string.dashboard_family; "category_clothing" -> R.string.dashboard_clothing; "category_taxes" -> R.string.dashboard_taxes; "category_salary" -> R.string.dashboard_salary; "category_freelance" -> R.string.dashboard_freelance; "category_pension" -> R.string.dashboard_pension; "category_benefits" -> R.string.dashboard_benefits; "category_gift" -> R.string.dashboard_gift; else -> R.string.dashboard_other })
@Composable private fun transactionTypeLabel(type: TransactionType) = stringResource(when(type) { TransactionType.EXPENSE -> R.string.dashboard_expense; TransactionType.INCOME -> R.string.dashboard_income_single; TransactionType.TRANSFER -> R.string.dashboard_transfer })
