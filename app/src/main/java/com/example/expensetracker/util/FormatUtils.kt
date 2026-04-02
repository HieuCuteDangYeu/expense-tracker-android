package com.example.expensetracker.util

import java.util.Locale

object FormatUtils {
    fun formatCurrency(amount: Double, currency: String = "USD"): String {
        val symbol = when {
            currency.contains("€") -> "€"
            currency.contains("£") -> "£"
            else -> "$"
        }
        return "$symbol${String.format(Locale.US, "%,.2f", amount)}"
    }
}
