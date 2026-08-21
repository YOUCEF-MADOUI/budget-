package com.budgetplusplus.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppFontFamily = FontFamily.SansSerif

val BalanceDisplayStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 40.sp,
    lineHeight = 48.sp,
    letterSpacing = (-0.5).sp,
)

val LargeAmountStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
)

val CompactAmountStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

val PercentageStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)

private fun budgetTextStyle(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
) = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
)

val BudgetPlusPlusTypography = Typography(
    displaySmall = BalanceDisplayStyle,
    headlineLarge = budgetTextStyle(FontWeight.Bold, 32, 40),
    headlineMedium = budgetTextStyle(FontWeight.SemiBold, 28, 36),
    headlineSmall = budgetTextStyle(FontWeight.SemiBold, 24, 32),
    titleLarge = budgetTextStyle(FontWeight.SemiBold, 22, 28),
    titleMedium = budgetTextStyle(FontWeight.SemiBold, 16, 24),
    titleSmall = budgetTextStyle(FontWeight.Medium, 14, 20),
    bodyLarge = budgetTextStyle(FontWeight.Normal, 16, 24),
    bodyMedium = budgetTextStyle(FontWeight.Normal, 14, 20),
    bodySmall = budgetTextStyle(FontWeight.Normal, 12, 16),
    labelLarge = budgetTextStyle(FontWeight.SemiBold, 14, 20),
    labelMedium = budgetTextStyle(FontWeight.Medium, 12, 16),
    labelSmall = budgetTextStyle(FontWeight.Medium, 11, 16),
)
