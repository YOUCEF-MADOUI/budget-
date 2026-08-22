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
import com.budgetplusplus.core.designsystem.demo.DesignSystemDemoScreen
import com.budgetplusplus.core.designsystem.icons.BudgetIcons
import com.budgetplusplus.feature.accounts.AccountsScreen
import com.budgetplusplus.feature.categories.CategoriesScreen
import com.budgetplusplus.feature.dashboard.HomePlaceholderScreen
import com.budgetplusplus.feature.onboarding.TechnicalWelcomeScreen
import com.budgetplusplus.feature.transactions.TransactionsScreen

@Composable
fun BudgetPlusPlusApp() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val mainRoutes = setOf(AppDestination.Home.route, AppDestination.Accounts.route, AppDestination.Transactions.route, AppDestination.Categories.route)
    val items = listOf(
        BudgetNavigationItem(AppDestination.Home.route, stringResource(R.string.nav_home), BudgetIcons.Home),
        BudgetNavigationItem(AppDestination.Accounts.route, stringResource(R.string.nav_accounts), BudgetIcons.Account),
        BudgetNavigationItem(AppDestination.Transactions.route, stringResource(R.string.nav_transactions), BudgetIcons.Search),
        BudgetNavigationItem(AppDestination.Categories.route, stringResource(R.string.nav_categories), BudgetIcons.Category),
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
                    onStartClick = {
                        navController.navigate(AppDestination.Home.route) {
                            popUpTo(AppDestination.Welcome.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(AppDestination.Home.route) {
                HomePlaceholderScreen(
                    onDesignSystemClick = { navController.navigate(AppDestination.DesignSystem.route) },
                    onAccountsClick = { navController.navigate(AppDestination.Accounts.route) },
                    onCategoriesClick = { navController.navigate(AppDestination.Categories.route) },
                    onTransactionsClick = { navController.navigate(AppDestination.Transactions.route) },
                )
            }
            composable(AppDestination.Accounts.route) { AccountsScreen(onBack = null) }
            composable(AppDestination.Categories.route) { CategoriesScreen(onBack = null) }
            composable(AppDestination.Transactions.route) { TransactionsScreen(onBack = null) }
            composable(AppDestination.DesignSystem.route) { DesignSystemDemoScreen(onBackClick = navController::navigateUp) }
        }
    }
}
