package com.example.expensetracker.data

/**
 * Data class returned by Room's grouped aggregation query.
 * Maps to: SELECT type AS expenseType, SUM(amount) AS totalAmount …
 */
data class ExpenseByType(
    val expenseType: String,
    val totalAmount: Double
)
