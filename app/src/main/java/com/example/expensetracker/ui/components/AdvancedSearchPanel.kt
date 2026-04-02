package com.example.expensetracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.ui.theme.AppTheme
import com.example.expensetracker.viewmodel.FilterState


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdvancedSearchPanel(
    visible: Boolean,
    filterState: FilterState,
    managers: List<String>,
    onStatusChange: (String?) -> Unit,
    onManagerChange: (String?) -> Unit,
    onStartDateChange: (String?) -> Unit,
    onEndDateChange: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ADVANCED SEARCH",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.extended.textSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "Clear",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.clickable { onClearFilters() }
                    )
                }

                // Date Range (2-column grid)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppDatePickerField(
                        label = "Start Date",
                        value = filterState.startDate,
                        onDateSelected = onStartDateChange,
                        modifier = Modifier.weight(1f),
                        showClearButton = true,
                        placeholder = "Select"
                    )
                    AppDatePickerField(
                        label = "End Date",
                        value = filterState.endDate,
                        onDateSelected = onEndDateChange,
                        modifier = Modifier.weight(1f),
                        showClearButton = true,
                        placeholder = "Select"
                    )
                }

                // Status chips
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "STATUS",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.extended.textTertiary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Active", "Completed", "On Hold").forEach { status ->
                            val isSelected = filterState.status == status
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onStatusChange(if (isSelected) null else status)
                                },
                                label = {
                                    Text(
                                        text = status,
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary
                                ),
                                border = if (!isSelected) {
                                    FilterChipDefaults.filterChipBorder(
                                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                                        selectedBorderColor = Color.Transparent,
                                        enabled = true,
                                        selected = false
                                    )
                                } else {
                                    FilterChipDefaults.filterChipBorder(
                                        borderColor = Color.Transparent,
                                        selectedBorderColor = Color.Transparent,
                                        enabled = true,
                                        selected = true
                                    )
                                },
                                leadingIcon = null
                            )
                        }
                    }
                }

                // Owner dropdown
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "OWNER",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.extended.textTertiary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    OwnerDropdown(
                        selectedManager = filterState.manager,
                        managers = managers,
                        onManagerChange = onManagerChange
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OwnerDropdown(
    selectedManager: String?,
    managers: List<String>,
    onManagerChange: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedManager ?: "All Owners",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            textStyle = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Owners", style = TextStyle(fontSize = 12.sp)) },
                onClick = {
                    onManagerChange(null)
                    expanded = false
                }
            )
            managers.forEach { manager ->
                DropdownMenuItem(
                    text = { Text(manager, style = TextStyle(fontSize = 12.sp)) },
                    onClick = {
                        onManagerChange(manager)
                        expanded = false
                    }
                )
            }
        }
    }
}
