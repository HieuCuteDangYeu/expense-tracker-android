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
import io.github.jan.supabase.storage.storage

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
    val receiptUri: String? = null,
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

    private var editingExpenseId: Int? = null

    fun loadExpenseForEdit(expense: ExpenseEntity) {
        editingExpenseId = expense.expenseId
        _projectId.value = expense.parentProjectId
        _formState.value = AddExpenseFormState(
            date = expense.date,
            amount = expense.amount.toString(),
            currency = expense.currency,
            category = expense.type,
            paymentMethod = expense.paymentMethod,
            status = expense.paymentStatus,
            claimant = expense.claimant,
            location = expense.location ?: "",
            description = expense.description ?: "",
            receiptUri = expense.receiptUrl,
            errors = emptyMap()
        )
    }

    private val _isLocationLoading = MutableStateFlow(false)
    val isLocationLoading: StateFlow<Boolean> = _isLocationLoading.asStateFlow()

    private val _showGpsDialog = MutableStateFlow(false)
    val showGpsDialog: StateFlow<Boolean> = _showGpsDialog.asStateFlow()

    fun dismissGpsDialog() {
        _showGpsDialog.value = false
    }

    fun setShowGpsDialog(show: Boolean) {
        _showGpsDialog.value = show
    }

    fun setLocationLoading(isLoading: Boolean) {
        _isLocationLoading.value = isLoading
    }

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

    fun setReceiptUri(uri: String?) {
        _formState.update { it.copy(receiptUri = uri) }
    }

    fun resetForm() {
        _formState.value = AddExpenseFormState()
        editingExpenseId = null
    }

    fun cacheImageLocally(context: android.content.Context, uri: android.net.Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = java.io.File(context.filesDir, "receipt_${java.util.UUID.randomUUID()}.jpg")
            file.outputStream().use { output -> inputStream.copyTo(output) }
            file.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("CacheError", "Failed to cache image", e)
            null
        }
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

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            var publicUrl: String? = currentState.receiptUri // fallback to local URI or null

            // If we have a local file path (not HTTP URL), attempt upload
            if (currentState.receiptUri != null && !currentState.receiptUri.startsWith("http")) {
                try {
                    val file = java.io.File(currentState.receiptUri)
                    if (file.exists()) {
                        val byteArray = file.readBytes()
                        
                        if (byteArray.isNotEmpty()) {
                            val uuid = UUID.randomUUID().toString()
                            val supabase = com.example.expensetracker.data.network.SupabaseClient.supabase
                            
                            // Upload to 'receipts' bucket
                            supabase.storage.from("receipts").upload("$uuid.jpg", byteArray)
                            
                            // Retrieve public URL
                            publicUrl = supabase.storage.from("receipts").publicUrl("$uuid.jpg")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ExpenseViewModel", "Failed to upload receipt", e)
                }
            }

            val expense = ExpenseEntity(
                expenseId = editingExpenseId ?: 0,
                parentProjectId = projectId,
                date = currentState.date,
                amount = currentState.amount.toDoubleOrNull() ?: 0.0,
                currency = currentState.currency,
                type = currentState.category,
                paymentMethod = currentState.paymentMethod,
                claimant = currentState.claimant,
                paymentStatus = currentState.status,
                description = currentState.description.ifBlank { null },
                location = currentState.location.ifBlank { null },
                receiptUrl = publicUrl
            )
            
            if (editingExpenseId != null) {
                expenseDao.updateExpense(expense)
            } else {
                expenseDao.insertExpense(expense)
            }
            
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
