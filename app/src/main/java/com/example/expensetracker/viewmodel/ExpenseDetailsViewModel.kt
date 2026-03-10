package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.ExpenseDao
import com.example.expensetracker.data.ExpenseEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseDetailsViewModel(
    private val expenseDao: ExpenseDao,
    expenseId: Int
) : ViewModel() {

    val expense: StateFlow<ExpenseEntity?> = expenseDao.getExpenseById(expenseId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun deleteExpense(onDeleted: () -> Unit) {
        val current = expense.value ?: return
        viewModelScope.launch {
            expenseDao.deleteExpense(current.expenseId)
            onDeleted()
        }
    }
}

class ExpenseDetailsViewModelFactory(
    private val expenseDao: ExpenseDao,
    private val expenseId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseDetailsViewModel(expenseDao, expenseId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
