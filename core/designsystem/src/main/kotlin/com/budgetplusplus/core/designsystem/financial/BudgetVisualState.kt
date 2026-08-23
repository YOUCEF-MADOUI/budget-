package com.budgetplusplus.core.designsystem.financial

import java.math.BigDecimal
import java.math.RoundingMode

enum class BudgetVisualState {
    Normal,
    NearLimit,
    Exceeded,
}

fun budgetUsedPercentage(spentMinor: Long, plannedMinor: Long): Int {
    if (plannedMinor <= 0L) return 0
    if (spentMinor <= 0L) return 0
    return BigDecimal.valueOf(spentMinor)
        .multiply(BigDecimal.valueOf(100L))
        .divide(BigDecimal.valueOf(plannedMinor), 0, RoundingMode.HALF_UP)
        .min(BigDecimal.valueOf(Int.MAX_VALUE.toLong()))
        .toInt()
}

fun budgetVisualState(
    spentMinor: Long,
    plannedMinor: Long,
    warningThresholdPercent: Int = 90,
): BudgetVisualState {
    val percentage = budgetUsedPercentage(spentMinor, plannedMinor)
    return when {
        plannedMinor > 0L && spentMinor > plannedMinor -> BudgetVisualState.Exceeded
        percentage >= warningThresholdPercent -> BudgetVisualState.NearLimit
        else -> BudgetVisualState.Normal
    }
}

internal fun budgetProgress(spentMinor: Long, plannedMinor: Long): Float {
    if (plannedMinor <= 0L || spentMinor <= 0L) return 0f
    return BigDecimal.valueOf(spentMinor)
        .divide(BigDecimal.valueOf(plannedMinor), 4, RoundingMode.HALF_UP)
        .coerceIn(BigDecimal.ZERO, BigDecimal.ONE)
        .toFloat()
}
