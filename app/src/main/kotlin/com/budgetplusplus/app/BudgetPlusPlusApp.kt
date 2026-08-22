package com.budgetplusplus.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.budgetplusplus.app.navigation.AppDestination
import com.budgetplusplus.core.designsystem.components.BudgetBottomNavigation
import com.budgetplusplus.core.designsystem.components.BudgetNavigationItem
import com.budgetplusplus.core.designsystem.icons.BudgetIcons
import com.budgetplusplus.feature.accounts.AccountsScreen
import com.budgetplusplus.feature.accounts.AccountDetailScreen
import com.budgetplusplus.feature.categories.CategoriesScreen
import com.budgetplusplus.feature.budgets.BudgetsScreen
import com.budgetplusplus.feature.recurring.RecurringScreen
import com.budgetplusplus.feature.analytics.AnalyticsScreen
import com.budgetplusplus.feature.search.SearchScreen
import com.budgetplusplus.feature.backup.BackupScreen
import com.budgetplusplus.feature.security.SecuritySettingsScreen
import com.budgetplusplus.feature.futurepayments.FuturePaymentsScreen
import com.budgetplusplus.feature.favorites.FavoritesScreen
import com.budgetplusplus.feature.dashboard.DashboardScreen
import com.budgetplusplus.feature.onboarding.TechnicalWelcomeScreen
import com.budgetplusplus.feature.transactions.TransactionsScreen
import com.budgetplusplus.feature.transactions.TransactionEditorScreen

@Composable
fun BudgetPlusPlusApp() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val mainRoutes = setOf(AppDestination.Home.route, AppDestination.Accounts.route, AppDestination.Transactions.route, AppDestination.Categories.route, AppDestination.Analytics.route)
    val items = listOf(
        BudgetNavigationItem(AppDestination.Home.route, stringResource(R.string.nav_home), BudgetIcons.Home),
        BudgetNavigationItem(AppDestination.Accounts.route, stringResource(R.string.nav_accounts), BudgetIcons.Account),
        BudgetNavigationItem(AppDestination.Transactions.route, stringResource(R.string.nav_transactions), BudgetIcons.Search),
        BudgetNavigationItem(AppDestination.Categories.route, stringResource(R.string.nav_categories), BudgetIcons.Category),
        BudgetNavigationItem(AppDestination.Analytics.route, stringResource(R.string.nav_analytics), BudgetIcons.Search),
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in mainRoutes) {
                BudgetBottomNavigation(
                    items = items,
                    selectedItemId = currentRoute.orEmpty(),
                    onItemSelected = { item ->
                        navController.navigate(item.id) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Welcome.route,
            modifier = Modifier.padding(contentPadding),
        ) {
            composable(AppDestination.Welcome.route) {
                TechnicalWelcomeScreen(
                    brandIconRes = R.mipmap.ic_launcher,
                    onStartClick = {
                        navController.navigate(AppDestination.Home.route) {
                            popUpTo(AppDestination.Welcome.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(AppDestination.Home.route) {
                DashboardScreen(
                    onAccountsClick = { navController.navigate(AppDestination.Accounts.route) },
                    onCategoriesClick = { navController.navigate(AppDestination.Categories.route) },
                    onTransactionsClick = { navController.navigate(AppDestination.Transactions.route) },
                    onBudgetsClick = { navController.navigate(AppDestination.Budgets.route) },
                    onRecurringClick = { navController.navigate(AppDestination.Recurring.route) },
                    onBackupClick = { navController.navigate(AppDestination.Backup.route) },
                    onSecurityClick = { navController.navigate(AppDestination.Security.route) },
                    onFuturePaymentsClick = { navController.navigate(AppDestination.FuturePayments.route) },
                )
            }
            composable(AppDestination.Accounts.route) { AccountsScreen(onBack = null, onAccountClick = { id -> navController.navigate("accounts/$id") }) }
            composable(AppDestination.AccountDetail.route) { entry -> AccountDetailScreen(entry.arguments?.getString("accountId").orEmpty(), navController::navigateUp, { id -> navController.navigate("transactions/edit/$id") }, { type, account -> navController.navigate("transactions/new/${type.name}/$account") }, { id -> navController.navigate("accounts/$id") }) }
            composable(AppDestination.Categories.route) { CategoriesScreen(onBack = null) }
            composable(AppDestination.Transactions.route) { TransactionsScreen(onBack = null, onAdvancedSearch = { navController.navigate(AppDestination.Search.route) }, onCreate = { navController.navigate(AppDestination.TransactionNew.route) }, onEdit = { id -> navController.navigate("transactions/edit/$id") }, onFavorites = { navController.navigate(AppDestination.Favorites.route) }) }
            composable(AppDestination.TransactionNew.route) { TransactionEditorScreen(null, navController::navigateUp) }
            composable(AppDestination.TransactionQuick.route) { entry -> TransactionEditorScreen(null, navController::navigateUp, initialType = entry.arguments?.getString("type")?.let { com.budgetplusplus.core.model.TransactionType.valueOf(it) }, initialAccountId = entry.arguments?.getString("accountId")) }
            composable(AppDestination.TransactionEdit.route) { entry -> TransactionEditorScreen(entry.arguments?.getString("transactionId"), navController::navigateUp) }
            composable(AppDestination.Budgets.route) { BudgetsScreen(onBack = navController::navigateUp) }
            composable(AppDestination.Recurring.route) { RecurringScreen(onBack = navController::navigateUp) }
            composable(AppDestination.Analytics.route) { AnalyticsScreen(onBack = null) }
            composable(AppDestination.Search.route) { SearchScreen(onBack = navController::navigateUp, onEdit = { id -> navController.navigate("transactions/edit/$id") }) }
            composable(AppDestination.Backup.route) { BackupScreen(onBack = navController::navigateUp) }
            composable(AppDestination.Security.route) { SecuritySettingsScreen(onBack = navController::navigateUp) }
            composable(AppDestination.FuturePayments.route) { FuturePaymentsScreen(onBack = navController::navigateUp) }
            composable(AppDestination.Favorites.route) { FavoritesScreen(onBack = navController::navigateUp) }
        }
    }
}
