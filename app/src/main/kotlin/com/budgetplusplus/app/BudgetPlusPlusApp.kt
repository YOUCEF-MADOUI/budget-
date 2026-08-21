package com.budgetplusplus.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.budgetplusplus.app.navigation.AppDestination
import com.budgetplusplus.core.designsystem.demo.DesignSystemDemoScreen
import com.budgetplusplus.feature.dashboard.HomePlaceholderScreen
import com.budgetplusplus.feature.accounts.AccountsScreen
import com.budgetplusplus.feature.categories.CategoriesScreen
import com.budgetplusplus.feature.transactions.TransactionsScreen
import com.budgetplusplus.feature.onboarding.TechnicalWelcomeScreen

@Composable
fun BudgetPlusPlusApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Welcome.route,
    ) {
        composable(AppDestination.Welcome.route) {
            TechnicalWelcomeScreen(
                onStartClick = {
                    navController.navigate(AppDestination.Home.route) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(AppDestination.Home.route) {
            HomePlaceholderScreen(
                onBackClick = navController::navigateUp,
                onDesignSystemClick = { navController.navigate(AppDestination.DesignSystem.route) },
                onAccountsClick = { navController.navigate(AppDestination.Accounts.route) },
                onCategoriesClick = { navController.navigate(AppDestination.Categories.route) },
                onTransactionsClick = { navController.navigate(AppDestination.Transactions.route) },
            )
        }
        composable(AppDestination.Accounts.route) { AccountsScreen(onBack = navController::navigateUp) }
        composable(AppDestination.Categories.route) { CategoriesScreen(onBack = navController::navigateUp) }
        composable(AppDestination.Transactions.route) { TransactionsScreen(onBack = navController::navigateUp) }
        composable(AppDestination.DesignSystem.route) {
            DesignSystemDemoScreen(onBackClick = navController::navigateUp)
        }
    }
}
