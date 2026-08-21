package com.budgetplusplus.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LightColorScheme = lightColorScheme(
    primary = BudgetGreen40,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = BudgetGreen90,
    onPrimaryContainer = BudgetGreen10,
    secondary = Slate40,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = Slate90,
    onSecondaryContainer = Slate10,
    tertiary = FinanceBlue40,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = FinanceBlue90,
    onTertiaryContainer = FinanceBlue20,
    background = Slate99,
    onBackground = Slate10,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = Slate10,
    surfaceVariant = Slate90,
    onSurfaceVariant = Slate30,
    surfaceContainer = Slate95,
    surfaceContainerLow = Slate98,
    surfaceContainerHigh = Slate90,
    outline = Slate40,
    outlineVariant = Slate80,
    error = Error40,
    onError = androidx.compose.ui.graphics.Color.White,
    errorContainer = Error90,
    onErrorContainer = Error20,
)

private val DarkColorScheme = darkColorScheme(
    primary = BudgetGreen80,
    onPrimary = BudgetGreen20,
    primaryContainer = BudgetGreen30,
    onPrimaryContainer = BudgetGreen90,
    secondary = Slate80,
    onSecondary = Slate20,
    secondaryContainer = Slate30,
    onSecondaryContainer = Slate90,
    tertiary = FinanceBlue80,
    onTertiary = FinanceBlue20,
    tertiaryContainer = FinanceBlue20,
    onTertiaryContainer = FinanceBlue90,
    background = Slate10,
    onBackground = Slate90,
    surface = ColorTokens.DarkSurface,
    onSurface = Slate90,
    surfaceVariant = Slate30,
    onSurfaceVariant = Slate80,
    surfaceContainer = Slate20,
    surfaceContainerLow = ColorTokens.DarkSurface,
    surfaceContainerHigh = Slate30,
    outline = Slate60,
    outlineVariant = Slate30,
    error = Error80,
    onError = Error20,
    errorContainer = ColorTokens.DarkErrorContainer,
    onErrorContainer = Error90,
)

private object ColorTokens {
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF181D1A)
    val DarkErrorContainer = androidx.compose.ui.graphics.Color(0xFF93000A)
}

private val LocalFinancialColors = staticCompositionLocalOf { LightFinancialColors }

val MaterialTheme.financialColors: FinancialColors
    @Composable get() = LocalFinancialColors.current

@Composable
fun BudgetPlusPlusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalFinancialColors provides if (darkTheme) DarkFinancialColors else LightFinancialColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = BudgetPlusPlusTypography,
            shapes = BudgetPlusPlusShapes,
            content = content,
        )
    }
}
