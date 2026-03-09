package com.example.expensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.ExpenseByType
import com.example.expensetracker.data.ExpenseDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Consolidated UI state consumed by the Insights screen.
 */
data class InsightsUiState(
    val totalExpenses: Double = 0.0,
    val totalBudget: Double = 0.0,
    val budgetUsedPercent: Int = 0,
    val isUnderBudget: Boolean = true,
    val expensesByType: List<ExpenseByType> = emptyList(),
    val topExpenseType: ExpenseByType? = null
)

class InsightsViewModel(
    application: Application,
    private val expenseDao: ExpenseDao
) : AndroidViewModel(application) {

    val uiState: StateFlow<InsightsUiState> = combine(
        expenseDao.getTotalExpenses(),
        expenseDao.getTotalBudget(),
        expenseDao.getExpensesByType(),
        expenseDao.getTopExpenseType()
    ) { totalExpenses, totalBudget, expensesByType, topExpenseType ->

        val budgetUsedPercent = if (totalBudget > 0) {
            ((totalExpenses / totalBudget) * 100).toInt()
        } else 0

        InsightsUiState(
            totalExpenses = totalExpenses,
            totalBudget = totalBudget,
            budgetUsedPercent = budgetUsedPercent,
            isUnderBudget = totalExpenses <= totalBudget,
            expensesByType = expensesByType,
            topExpenseType = topExpenseType
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState()
    )
}

class InsightsViewModelFactory(
    private val application: Application,
    private val expenseDao: ExpenseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InsightsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InsightsViewModel(application, expenseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
