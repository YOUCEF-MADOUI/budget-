package com.budgetplusplus.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = BudgetGreen,
    onPrimary = OnBudgetGreen,
    tertiary = BudgetBlue,
    background = LightBackground,
    surface = androidx.compose.ui.graphics.Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = BudgetGreenDark,
    onPrimary = OnBudgetGreenDark,
    tertiary = BudgetBlueDark,
    background = DarkBackground,
    surface = DarkSurface,
)

@Composable
fun BudgetPlusPlusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = BudgetPlusPlusTypography,
        shapes = BudgetPlusPlusShapes,
        content = content,
    )
}
