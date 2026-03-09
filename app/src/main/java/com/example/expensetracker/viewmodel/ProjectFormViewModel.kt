package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.ProjectDao
import com.example.expensetracker.data.ProjectEntity
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProjectFormState(
        val projectId: String = "PRJ-${UUID.randomUUID().toString().take(6).uppercase()}",
        val projectName: String = "",
        val description: String = "",
        val startDate: String = "",
        val endDate: String = "",
        val manager: String = "",
        val status: String = "Active",
        val budget: String = "",
        val specialRequirements: String = "", // Optional
        val clientInfo: String = "", // Optional
        val priority: String = "Medium",
        var errors: Map<String, String> = emptyMap()
)

class ProjectFormViewModel(private val projectDao: ProjectDao) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectFormState())
    val uiState: StateFlow<ProjectFormState> = _uiState.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private var editingProjectId: Int? = null

    fun updateField(field: String, value: String) {
        _uiState.update { currentState ->
            val newErrors = currentState.errors.toMutableMap()
            newErrors.remove(field) // Clear error on change

            when (field) {
                "projectName" -> currentState.copy(projectName = value, errors = newErrors)
                "description" -> currentState.copy(description = value, errors = newErrors)
                "startDate" -> currentState.copy(startDate = value, errors = newErrors)
                "endDate" -> currentState.copy(endDate = value, errors = newErrors)
                "manager" -> currentState.copy(manager = value, errors = newErrors)
                "status" -> currentState.copy(status = value, errors = newErrors)
                "budget" -> currentState.copy(budget = value, errors = newErrors)
                "specialRequirements" ->
                        currentState.copy(specialRequirements = value, errors = newErrors)
                "clientInfo" -> currentState.copy(clientInfo = value, errors = newErrors)
                "priority" -> currentState.copy(priority = value, errors = newErrors)
                else -> currentState
            }
        }
    }

    fun validate(): Boolean {
        val currentState = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (currentState.projectName.isBlank())
                errors["projectName"] = "Please enter the Project Name"
        if (currentState.description.isBlank()) errors["description"] = "Please enter a Description"
        if (currentState.startDate.isBlank()) errors["startDate"] = "Please select a Start Date"
        if (currentState.endDate.isBlank()) errors["endDate"] = "Please select an End Date"
        if (currentState.manager.isBlank()) errors["manager"] = "Please assign a Manager"
        if (currentState.budget.isBlank() || currentState.budget.toDoubleOrNull() == null) {
            errors["budget"] = "Please enter a valid numeric budget"
        }

        _uiState.update { it.copy(errors = errors) }
        return errors.isEmpty()
    }

    fun resetForm() {
        _uiState.value = ProjectFormState()
        _isEditMode.value = false
        editingProjectId = null
    }

    fun loadProject(projectId: Int) {
        viewModelScope.launch {
            projectDao.getProjectWithExpenses(projectId).collect { projectWithExpenses ->
                val project = projectWithExpenses.project
                _uiState.value =
                        ProjectFormState(
                                projectId = project.formattedId,
                                projectName = project.projectName,
                                description = project.description,
                                startDate = project.startDate,
                                endDate = project.endDate,
                                manager = project.manager,
                                status = project.status,
                                budget = project.budget.toString(),
                                specialRequirements = project.specialRequirements ?: "",
                                clientInfo = project.clientInfo ?: "",
                                priority = project.priority,
                                errors = emptyMap()
                        )
                _isEditMode.value = true
                editingProjectId = project.projectId
            }
        }
    }

    fun saveProject(onSuccess: () -> Unit) {
        if (!validate()) return

        val state = _uiState.value
        val isEditing = _isEditMode.value

        // If editing, use the existing ID. Otherwise generate a new hash.
        val targetId =
                if (isEditing && editingProjectId != null) {
                    editingProjectId!!
                } else {
                    kotlin.math.abs(state.projectId.hashCode())
                }

        val projectToSave =
                ProjectEntity(
                        projectId = targetId,
                        projectName = state.projectName,
                        description = state.description,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        manager = state.manager,
                        status = state.status,
                        budget = state.budget.toDouble(),
                        specialRequirements = state.specialRequirements.takeIf { it.isNotBlank() },
                        clientInfo = state.clientInfo.takeIf { it.isNotBlank() },
                        priority = state.priority
                )

        viewModelScope.launch {
            if (isEditing) {
                projectDao.updateProject(projectToSave)
            } else {
                projectDao.insertProject(projectToSave)
            }
            resetForm()
            onSuccess()
        }
    }
}

class ProjectFormViewModelFactory(private val projectDao: ProjectDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ProjectFormViewModel(projectDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
