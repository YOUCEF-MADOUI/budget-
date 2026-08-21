package com.budgetplusplus.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Brand primitives are private: screens consume MaterialTheme and FinancialColors only.
internal val BudgetGreen10 = Color(0xFF00201A)
internal val BudgetGreen20 = Color(0xFF00382E)
internal val BudgetGreen30 = Color(0xFF005143)
internal val BudgetGreen40 = Color(0xFF176B5B)
internal val BudgetGreen80 = Color(0xFF72D6BE)
internal val BudgetGreen90 = Color(0xFF91F3DA)
internal val BudgetGreen95 = Color(0xFFB9FFE9)
internal val BudgetGreen99 = Color(0xFFF3FFF9)

internal val Slate10 = Color(0xFF171D1A)
internal val Slate20 = Color(0xFF2B322F)
internal val Slate30 = Color(0xFF414946)
internal val Slate40 = Color(0xFF59615E)
internal val Slate60 = Color(0xFF8A938F)
internal val Slate80 = Color(0xFFC0C9C5)
internal val Slate90 = Color(0xFFDCE5E1)
internal val Slate95 = Color(0xFFEBF3EF)
internal val Slate98 = Color(0xFFF5FAF7)
internal val Slate99 = Color(0xFFF8FCF9)

internal val FinanceBlue40 = Color(0xFF365E8D)
internal val FinanceBlue80 = Color(0xFFA5C8F8)
internal val FinanceBlue90 = Color(0xFFD2E4FF)
internal val FinanceBlue20 = Color(0xFF07305F)
internal val Error40 = Color(0xFFB3261E)
internal val Error80 = Color(0xFFFFB4AB)
internal val Error90 = Color(0xFFFFDAD6)
internal val Error20 = Color(0xFF690005)

@Immutable
data class FinancialColors(
    val income: Color,
    val onIncome: Color,
    val incomeContainer: Color,
    val onIncomeContainer: Color,
    val expense: Color,
    val onExpense: Color,
    val expenseContainer: Color,
    val onExpenseContainer: Color,
    val transfer: Color,
    val transferContainer: Color,
    val onTransferContainer: Color,
    val savings: Color,
    val savingsContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val budgetNearLimit: Color,
    val budgetExceeded: Color,
    val disabled: Color,
    val chartSeries: List<Color>,
)

internal val LightFinancialColors = FinancialColors(
    income = Color(0xFF087A55),
    onIncome = Color.White,
    incomeContainer = Color(0xFFD0F8E5),
    onIncomeContainer = Color(0xFF002116),
    expense = Error40,
    onExpense = Color.White,
    expenseContainer = Error90,
    onExpenseContainer = Error20,
    transfer = FinanceBlue40,
    transferContainer = FinanceBlue90,
    onTransferContainer = FinanceBlue20,
    savings = Color(0xFF6750A4),
    savingsContainer = Color(0xFFEADDFF),
    warning = Color(0xFF8A4F00),
    warningContainer = Color(0xFFFFDDB3),
    onWarningContainer = Color(0xFF2C1600),
    budgetNearLimit = Color(0xFF8A4F00),
    budgetExceeded = Error40,
    disabled = Slate60,
    chartSeries = listOf(BudgetGreen40, FinanceBlue40, Color(0xFF7A5900), Color(0xFF6750A4), Color(0xFF8E4E62)),
)

internal val DarkFinancialColors = FinancialColors(
    income = Color(0xFF68DBAC),
    onIncome = Color(0xFF003825),
    incomeContainer = Color(0xFF005138),
    onIncomeContainer = Color(0xFF8EF8C7),
    expense = Error80,
    onExpense = Error20,
    expenseContainer = Color(0xFF93000A),
    onExpenseContainer = Error90,
    transfer = FinanceBlue80,
    transferContainer = FinanceBlue20,
    onTransferContainer = FinanceBlue90,
    savings = Color(0xFFD0BCFF),
    savingsContainer = Color(0xFF4F378B),
    warning = Color(0xFFFFB95C),
    warningContainer = Color(0xFF663C00),
    onWarningContainer = Color(0xFFFFDDB3),
    budgetNearLimit = Color(0xFFFFB95C),
    budgetExceeded = Error80,
    disabled = Slate60,
    chartSeries = listOf(BudgetGreen80, FinanceBlue80, Color(0xFFFFC44F), Color(0xFFD0BCFF), Color(0xFFFFB0C8)),
)
