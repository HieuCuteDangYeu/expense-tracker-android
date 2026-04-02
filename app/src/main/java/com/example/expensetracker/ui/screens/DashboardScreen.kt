package com.example.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ui.components.AdvancedSearchPanel
import com.example.expensetracker.ui.components.EmptyStateMessage
import com.example.expensetracker.ui.components.ProjectCard
import com.example.expensetracker.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
        viewModel: ProjectViewModel,
        onProjectClick: (Int) -> Unit,
        onEditProject: (Int) -> Unit,
        modifier: Modifier = Modifier
) {
        val searchQuery by viewModel.searchQuery.collectAsState()
        val projects by viewModel.projects.collectAsState()
        val showFilterPanel by viewModel.showFilterPanel.collectAsState()
        val filterState by viewModel.filterState.collectAsState()
        val managers by viewModel.managers.collectAsState()

        Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                // Sticky Search & Filter Bar
                Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp
                ) {
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(
                                                        start = 16.dp,
                                                        end = 16.dp,
                                                        bottom = 12.dp
                                                ),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { viewModel.onSearchQueryChange(it) },
                                        modifier =
                                                Modifier.weight(1f)
                                                        .background(
                                                                MaterialTheme.colorScheme.surface,
                                                                RoundedCornerShape(8.dp)
                                                        ),
                                        placeholder = {
                                                Text(
                                                        "Search projects",
                                                        style = MaterialTheme.typography.bodyMedium
                                                )
                                        },
                                        leadingIcon = {
                                                Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = "Search",
                                                        tint =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        colors =
                                                OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor =
                                                                MaterialTheme.colorScheme.primary,
                                                        unfocusedBorderColor =
                                                                MaterialTheme.colorScheme.outline
                                                )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Advanced Filter Button
                                Button(
                                        onClick = { viewModel.toggleFilterPanel() },
                                        shape = RoundedCornerShape(8.dp),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor =
                                                                if (showFilterPanel)
                                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                                                else
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                ),
                                        contentPadding =
                                                PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                        Text(
                                                "Filter",
                                                style =
                                                        MaterialTheme.typography.labelMedium.copy(
                                                                fontWeight = FontWeight.Bold
                                                        )
                                        )
                                }
                        }
                }

                // Advanced Search Panel (toggled by Filter button)
                AdvancedSearchPanel(
                        visible = showFilterPanel,
                        filterState = filterState,
                        managers = managers,
                        onStatusChange = { viewModel.updateStatusFilter(it) },
                        onManagerChange = { viewModel.updateManagerFilter(it) },
                        onStartDateChange = { viewModel.updateStartDateFilter(it) },
                        onEndDateChange = { viewModel.updateEndDateFilter(it) },
                        onClearFilters = { viewModel.clearFilters() }
                )

                // Project List
                LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        if (projects.isEmpty()) {
                                item {
                                        EmptyStateMessage(
                                                title = "No projects found",
                                                modifier = Modifier.padding(16.dp)
                                        )
                                }
                        } else {
                                items(projects, key = { it.project.projectId }) {
                                        projectWithExpenses ->
                                        ProjectCard(
                                                projectWithExpenses = projectWithExpenses,
                                                onClick = {
                                                        onProjectClick(
                                                                projectWithExpenses
                                                                        .project
                                                                        .projectId
                                                        )
                                                },
                                                onEdit = {
                                                        onEditProject(
                                                                projectWithExpenses
                                                                        .project
                                                                        .projectId
                                                        )
                                                },
                                                onDelete = {
                                                        viewModel.deleteProject(
                                                                projectWithExpenses.project
                                                        )
                                                }
                                        )
                                }
                        }
                }
        }
}
