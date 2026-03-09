package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.ExpenseDao
import com.example.expensetracker.data.ExpenseEntity
import com.example.expensetracker.data.ProjectDao
import com.example.expensetracker.data.ProjectWithExpenses
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AddExpenseFormState(
    val date: String = "",
    val amount: String = "",
    val currency: String = "USD ($)",
    val category: String = "Materials",
    val paymentMethod: String = "Cash",
    val status: String = "Pending",
    val claimant: String = "",
    val location: String = "",
    val description: String = "",
    val errors: Map<String, String> = emptyMap()
)

class ExpenseViewModel(
    private val projectDao: ProjectDao,
    private val expenseDao: ExpenseDao
) : ViewModel() {

    private val _projectId = MutableStateFlow<Int?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val projectDetails: StateFlow<ProjectWithExpenses?> = _projectId
        .filterNotNull()
        .flatMapLatest { id ->
            projectDao.getProjectWithExpenses(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allExpenses: StateFlow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _formState = MutableStateFlow(AddExpenseFormState())
    val formState: StateFlow<AddExpenseFormState> = _formState.asStateFlow()

    fun setProjectId(id: Int) {
        _projectId.value = id
    }

    fun updateField(field: String, value: String) {
        _formState.update { state ->
            val updatedErrors = state.errors.toMutableMap()
            updatedErrors.remove(field)

            when (field) {
                "date" -> state.copy(date = value, errors = updatedErrors)
                "amount" -> state.copy(amount = value, errors = updatedErrors)
                "currency" -> state.copy(currency = value, errors = updatedErrors)
                "category" -> state.copy(category = value, errors = updatedErrors)
                "paymentMethod" -> state.copy(paymentMethod = value, errors = updatedErrors)
                "status" -> state.copy(status = value, errors = updatedErrors)
                "claimant" -> state.copy(claimant = value, errors = updatedErrors)
                "location" -> state.copy(location = value, errors = updatedErrors)
                "description" -> state.copy(description = value, errors = updatedErrors)
                else -> state
            }
        }
    }

    fun resetForm() {
        _formState.value = AddExpenseFormState()
    }

    fun saveExpense(): Boolean {
        val currentState = _formState.value
        val errors = mutableMapOf<String, String>()

        if (currentState.date.isBlank()) errors["date"] = "Date is required"
        if (currentState.amount.isBlank() || currentState.amount.toDoubleOrNull() == null) errors["amount"] = "Valid amount is required"
        if (currentState.claimant.isBlank()) errors["claimant"] = "Claimant is required"

        if (errors.isNotEmpty()) {
            _formState.update { it.copy(errors = errors) }
            return false
        }

        val projectId = _projectId.value ?: return false

        viewModelScope.launch {
            val expense = ExpenseEntity(
                parentProjectId = projectId,
                date = currentState.date,
                amount = currentState.amount.toDoubleOrNull() ?: 0.0,
                currency = currentState.currency,
                type = currentState.category,
                paymentMethod = currentState.paymentMethod,
                claimant = currentState.claimant,
                paymentStatus = currentState.status,
                description = currentState.description.ifBlank { null },
                location = currentState.location.ifBlank { null }
            )
            expenseDao.insertExpense(expense)
            resetForm()
        }
        return true
    }
}

class ExpenseViewModelFactory(
    private val projectDao: ProjectDao,
    private val expenseDao: ExpenseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(projectDao, expenseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
