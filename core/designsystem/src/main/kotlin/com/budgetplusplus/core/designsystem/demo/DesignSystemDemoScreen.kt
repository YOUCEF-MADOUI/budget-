package com.budgetplusplus.core.designsystem.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.budgetplusplus.core.designsystem.R
import com.budgetplusplus.core.designsystem.components.BudgetAddFab
import com.budgetplusplus.core.designsystem.components.BudgetAmountField
import com.budgetplusplus.core.designsystem.components.BudgetBottomNavigation
import com.budgetplusplus.core.designsystem.components.BudgetConfirmationDialog
import com.budgetplusplus.core.designsystem.components.BudgetEmptyState
import com.budgetplusplus.core.designsystem.components.BudgetErrorState
import com.budgetplusplus.core.designsystem.components.BudgetFilterChip
import com.budgetplusplus.core.designsystem.components.BudgetNavigationItem
import com.budgetplusplus.core.designsystem.components.BudgetOfflineState
import com.budgetplusplus.core.designsystem.components.BudgetPrimaryButton
import com.budgetplusplus.core.designsystem.components.BudgetSearchBar
import com.budgetplusplus.core.designsystem.components.BudgetSecondaryButton
import com.budgetplusplus.core.designsystem.components.BudgetSelector
import com.budgetplusplus.core.designsystem.components.BudgetSkeleton
import com.budgetplusplus.core.designsystem.components.BudgetSnackbarHost
import com.budgetplusplus.core.designsystem.components.BudgetTextButton
import com.budgetplusplus.core.designsystem.components.BudgetTopAppBar
import com.budgetplusplus.core.designsystem.financial.AccountCard
import com.budgetplusplus.core.designsystem.financial.AccountVisualState
import com.budgetplusplus.core.designsystem.financial.AmountTone
import com.budgetplusplus.core.designsystem.financial.BalanceCard
import com.budgetplusplus.core.designsystem.financial.FinancialBudgetCard
import com.budgetplusplus.core.designsystem.financial.IncomeExpenseCard
import com.budgetplusplus.core.designsystem.financial.MoneyText
import com.budgetplusplus.core.designsystem.financial.TransactionRow
import com.budgetplusplus.core.designsystem.financial.TransactionVisualType
import com.budgetplusplus.core.designsystem.icons.BudgetIcons
import com.budgetplusplus.core.designsystem.theme.BudgetPlusPlusTheme
import com.budgetplusplus.core.designsystem.theme.financialColors
import com.budgetplusplus.core.designsystem.tokens.BudgetSizes
import com.budgetplusplus.core.designsystem.tokens.BudgetSpacing
import kotlinx.coroutines.launch

@Composable
fun DesignSystemDemoScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var darkTheme by rememberSaveable { mutableStateOf(false) }
    BudgetPlusPlusTheme(darkTheme = darkTheme) {
        DesignSystemDemoContent(
            darkTheme = darkTheme,
            onThemeToggle = { darkTheme = !darkTheme },
            onBackClick = onBackClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun DesignSystemDemoContent(
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var amount by rememberSaveable { mutableStateOf("5800") }
    var search by rememberSaveable { mutableStateOf("") }
    var balanceHidden by rememberSaveable { mutableStateOf(false) }
    var selectedFilter by rememberSaveable { mutableStateOf(true) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val message = stringResource(R.string.ds_demo_message)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BudgetTopAppBar(
                title = stringResource(R.string.ds_demo_title),
                onBackClick = onBackClick,
            )
        },
        snackbarHost = { BudgetSnackbarHost(snackbarHostState) },
        floatingActionButton = { BudgetAddFab(onClick = {}, expanded = false) },
        bottomBar = {
            BudgetBottomNavigation(
                items = listOf(
                    BudgetNavigationItem("home", stringResource(R.string.ds_demo_home), Icons.Default.Home),
                    BudgetNavigationItem("accounts", stringResource(R.string.ds_demo_accounts), BudgetIcons.Account),
                    BudgetNavigationItem("analysis", stringResource(R.string.ds_demo_analysis), Icons.Default.Search),
                ),
                selectedItemId = "home",
                onItemSelected = {},
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Md),
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = BudgetSpacing.Md),
                    verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Xs),
                ) {
                    Text(stringResource(R.string.ds_demo_subtitle), style = MaterialTheme.typography.bodyLarge)
                    BudgetSecondaryButton(
                        text = stringResource(
                            if (darkTheme) R.string.ds_demo_light_theme else R.string.ds_demo_dark_theme,
                        ),
                        onClick = onThemeToggle,
                    )
                }
            }
            item { DemoColors() }
            item { DemoTypography() }
            item {
                DemoSection(stringResource(R.string.ds_demo_actions)) {
                    BudgetPrimaryButton(
                        text = stringResource(R.string.ds_demo_primary),
                        onClick = { showDialog = true },
                        leadingIcon = BudgetIcons.Add,
                    )
                    BudgetSecondaryButton(stringResource(R.string.ds_demo_secondary), onClick = {})
                    BudgetTextButton(stringResource(R.string.ds_demo_text_button), onClick = {})
                    BudgetPrimaryButton(
                        text = stringResource(R.string.ds_demo_loading_button),
                        onClick = {},
                        loading = true,
                    )
                    BudgetFilterChip(
                        text = stringResource(R.string.ds_demo_filter),
                        selected = selectedFilter,
                        onClick = { selectedFilter = !selectedFilter },
                    )
                }
            }
            item {
                DemoSection(stringResource(R.string.ds_demo_fields)) {
                    BudgetAmountField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = stringResource(R.string.ds_demo_amount_label),
                        currencyCode = "DZD",
                    )
                    BudgetSelector(
                        label = stringResource(R.string.ds_demo_account_label),
                        value = stringResource(R.string.ds_demo_account_value),
                        onClick = {},
                    )
                    BudgetSearchBar(
                        query = search,
                        onQueryChange = { search = it },
                        placeholder = stringResource(R.string.ds_demo_search),
                    )
                }
            }
            item { DemoFinancialComponents(balanceHidden) { balanceHidden = !balanceHidden } }
            item {
                DemoSection(stringResource(R.string.ds_demo_feedback)) {
                    BudgetEmptyState(
                        title = stringResource(R.string.ds_demo_empty_title),
                        message = stringResource(R.string.ds_demo_empty_message),
                        actionText = stringResource(R.string.ds_add),
                        onAction = {},
                    )
                    BudgetErrorState(
                        title = stringResource(R.string.ds_demo_error_title),
                        message = stringResource(R.string.ds_demo_error_message),
                        onRetry = {},
                    )
                    BudgetOfflineState(onRetry = {})
                    BudgetSkeleton(lines = 3)
                    BudgetSecondaryButton(
                        text = stringResource(R.string.ds_demo_show_message),
                        onClick = { scope.launch { snackbarHostState.showSnackbar(message) } },
                    )
                }
            }
        }
    }

    if (showDialog) {
        BudgetConfirmationDialog(
            title = stringResource(R.string.ds_demo_dialog_title),
            message = stringResource(R.string.ds_demo_dialog_message),
            onConfirm = { showDialog = false },
            onDismiss = { showDialog = false },
            destructive = true,
        )
    }
}

@Composable
private fun DemoColors() {
    val financial = MaterialTheme.financialColors
    DemoSection(stringResource(R.string.ds_demo_colors)) {
        listOf(
            stringResource(R.string.ds_demo_income_color) to financial.income,
            stringResource(R.string.ds_demo_expense_color) to financial.expense,
            stringResource(R.string.ds_demo_transfer_color) to financial.transfer,
            stringResource(R.string.ds_demo_warning_color) to financial.warning,
        ).forEach { (label, color) -> ColorSwatch(label, color) }
    }
}

@Composable
private fun ColorSwatch(label: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BudgetSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(BudgetSizes.CategoryIcon),
            color = color,
            shape = MaterialTheme.shapes.medium,
        ) {}
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun DemoTypography() {
    DemoSection(stringResource(R.string.ds_demo_typography)) {
        Text(stringResource(R.string.ds_demo_balance_title), style = MaterialTheme.typography.headlineSmall)
        MoneyText(124_560_000L, "DZD", tone = AmountTone.Neutral, style = MaterialTheme.typography.displaySmall)
        MoneyText(450_000_00L, "DZD", tone = AmountTone.Income)
        MoneyText(-5_800_00L, "DZD", tone = AmountTone.Expense)
        MoneyText(125_000L, "EUR", tone = AmountTone.Neutral)
        MoneyText(125_000L, "USD", tone = AmountTone.Neutral)
    }
}

@Composable
private fun DemoFinancialComponents(hidden: Boolean, onToggleHidden: () -> Unit) {
    DemoSection(stringResource(R.string.ds_demo_finance)) {
        BalanceCard(
            title = stringResource(R.string.ds_demo_balance_title),
            balanceMinor = 124_560_000L,
            currencyCode = "DZD",
            hidden = hidden,
            variation = stringResource(R.string.ds_demo_variation),
            onToggleVisibility = onToggleHidden,
        )
        IncomeExpenseCard(
            period = stringResource(R.string.ds_demo_period),
            incomeLabel = stringResource(R.string.ds_demo_income),
            expenseLabel = stringResource(R.string.ds_demo_expense),
            differenceLabel = stringResource(R.string.ds_demo_difference),
            incomeMinor = 45_000_000L,
            expenseMinor = 28_700_000L,
            currencyCode = "DZD",
        )
        TransactionRow(
            category = stringResource(R.string.ds_demo_category),
            description = stringResource(R.string.ds_demo_description),
            date = stringResource(R.string.ds_demo_date),
            account = stringResource(R.string.ds_demo_account_value),
            amountMinor = -580_000L,
            currencyCode = "DZD",
            type = TransactionVisualType.Expense,
            categoryIcon = BudgetIcons.Category,
            recurring = true,
        )
        FinancialBudgetCard(
            name = stringResource(R.string.ds_demo_budget_name),
            plannedLabel = stringResource(R.string.ds_demo_planned),
            spentLabel = stringResource(R.string.ds_demo_spent),
            remainingLabel = stringResource(R.string.ds_demo_remaining),
            plannedMinor = 5_000_000L,
            spentMinor = 4_600_000L,
            currencyCode = "DZD",
        )
        AccountCard(
            name = stringResource(R.string.ds_demo_account_value),
            type = stringResource(R.string.ds_demo_account_type),
            balanceMinor = 54_000_000L,
            currencyCode = "DZD",
            icon = BudgetIcons.Account,
            state = AccountVisualState.Active,
            onClick = {},
        )
    }
}

@Composable
private fun DemoSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = BudgetSpacing.Md),
        verticalArrangement = Arrangement.spacedBy(BudgetSpacing.Sm),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        content()
    }
}
