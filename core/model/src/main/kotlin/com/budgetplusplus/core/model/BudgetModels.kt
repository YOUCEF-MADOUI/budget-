package com.budgetplusplus.core.model

import java.math.BigInteger

enum class BudgetScope { GLOBAL, CATEGORY }
enum class BudgetPeriodType { MONTHLY, CUSTOM }
enum class BudgetStatus { NORMAL, NEAR_LIMIT, EXCEEDED }

data class BudgetInput(
    val id: String? = null,
    val name: String,
    val scope: BudgetScope,
    val categoryId: String? = null,
    val amountMinor: Long,
    val currencyCode: String = "DZD",
    val periodType: BudgetPeriodType,
    val startDate: String,
    val endDate: String,
    val warningThresholdPercent: Int = 90,
)

data class BudgetProgress(
    val id: String,
    val name: String,
    val scope: BudgetScope,
    val categoryId: String? = null,
    val categoryNameKey: String? = null,
    val categoryCustomName: String? = null,
    val amountMinor: Long,
    val spentMinor: Long,
    val currencyCode: String,
    val periodType: BudgetPeriodType,
    val startDate: String,
    val endDate: String,
    val warningThresholdPercent: Int,
    val isArchived: Boolean,
) {
    val remainingMinor: Long get() = runCatching { Math.subtractExact(amountMinor, spentMinor) }.getOrElse { Long.MIN_VALUE }
    val status: BudgetStatus get() = when {
        spentMinor > amountMinor -> BudgetStatus.EXCEEDED
        amountMinor > 0 && BigInteger.valueOf(spentMinor).multiply(BigInteger.valueOf(100)) >= BigInteger.valueOf(amountMinor).multiply(BigInteger.valueOf(warningThresholdPercent.toLong())) -> BudgetStatus.NEAR_LIMIT
        else -> BudgetStatus.NORMAL
    }
}
