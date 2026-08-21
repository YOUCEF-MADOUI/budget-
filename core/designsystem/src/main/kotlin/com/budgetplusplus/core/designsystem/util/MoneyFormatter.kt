package com.budgetplusplus.core.designsystem.util

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object MoneyFormatter {
    fun format(
        minorUnits: Long,
        currencyCode: String,
        locale: Locale = Locale.getDefault(),
        showPositiveSign: Boolean = false,
    ): String {
        val currency = currency(currencyCode)
        val fractionDigits = currency.defaultFractionDigits.coerceAtLeast(0)
        val amount = BigDecimal.valueOf(minorUnits).movePointLeft(fractionDigits)
        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            this.currency = currency
            minimumFractionDigits = fractionDigits
            maximumFractionDigits = fractionDigits
        }
        if (formatter is DecimalFormat && currency.currencyCode == "DZD") {
            formatter.decimalFormatSymbols = formatter.decimalFormatSymbols.apply {
                currencySymbol = "DA"
            }
        }
        val formatted = formatter.format(amount)
        return if (showPositiveSign && minorUnits > 0L) "+$formatted" else formatted
    }

    fun fractionDigits(currencyCode: String): Int =
        currency(currencyCode).defaultFractionDigits.coerceAtLeast(0)

    private fun currency(currencyCode: String): Currency {
        require(currencyCode.matches(Regex("[A-Za-z]{3}"))) {
            "Currency code must contain exactly three letters"
        }
        return Currency.getInstance(currencyCode.uppercase(Locale.ROOT))
    }
}
