package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.ProjectDao
import com.example.expensetracker.data.ProjectEntity
import com.example.expensetracker.data.ProjectWithExpenses
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FilterState(
    val status: String? = null,
    val manager: String? = null,
    val startDate: String? = null,
    val endDate: String? = null
)

class ProjectViewModel(private val projectDao: ProjectDao) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _showFilterPanel = MutableStateFlow(false)
    val showFilterPanel: StateFlow<Boolean> = _showFilterPanel.asStateFlow()

    val managers: StateFlow<List<String>> = projectDao.getAllManagers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val projects: StateFlow<List<ProjectWithExpenses>> =
        combine(_searchQuery, _filterState) { query, filters ->
            Pair(query, filters)
        }.flatMapLatest { (query, filters) ->
            val q = query.ifBlank { null }
            projectDao.filterProjects(
                query = q,
                status = filters.status,
                manager = filters.manager,
                startDate = filters.startDate,
                endDate = filters.endDate
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleFilterPanel() {
        _showFilterPanel.value = !_showFilterPanel.value
    }

    fun updateStatusFilter(status: String?) {
        _filterState.value = _filterState.value.copy(status = status)
    }

    fun updateManagerFilter(manager: String?) {
        _filterState.value = _filterState.value.copy(manager = manager)
    }

    fun updateStartDateFilter(startDate: String?) {
        _filterState.value = _filterState.value.copy(startDate = startDate)
    }

    fun updateEndDateFilter(endDate: String?) {
        _filterState.value = _filterState.value.copy(endDate = endDate)
    }

    fun clearFilters() {
        _filterState.value = FilterState()
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch { projectDao.deleteProject(project.projectId) }
    }
}

class ProjectViewModelFactory(private val projectDao: ProjectDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ProjectViewModel(projectDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
