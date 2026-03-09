package com.example.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.ui.theme.AppTheme
import com.example.expensetracker.viewmodel.ProjectFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectScreen(viewModel: ProjectFormViewModel, onNavigateBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var showReviewDialog by remember { mutableStateOf(false) }

    if (showReviewDialog) {
        ReviewDetailsBottomSheet(
                viewModel = viewModel,
                onDismiss = { showReviewDialog = false },
                onConfirmAndSave = {
                    showReviewDialog = false
                    viewModel.resetForm()
                    onNavigateBack()
                }
        )
    }

    Scaffold(
            topBar = {
                // Handled in MainScreen for routing title dynamically
            },
            contentWindowInsets = WindowInsets(0.dp),
            bottomBar = {
                // Action Buttons Footer Node (p-4 border-t gap-3)
                Column {
                    HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                    )
                    Row(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(16.dp), // p-4
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                                onClick = {
                                    viewModel.resetForm()
                                    onNavigateBack()
                                },
                                shape = RoundedCornerShape(8.dp),
                                border =
                                        androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant
                                        ),
                                colors =
                                        ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                modifier =
                                        Modifier.padding(end = 12.dp)
                                                .height(40.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Text(
                                    "Cancel",
                                    style =
                                            androidx.compose.ui.text.TextStyle(
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium
                                            )
                            )
                        }

                        Button(
                                onClick = {
                                    if (viewModel.validate()) {
                                        showReviewDialog = true
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                modifier = Modifier.height(40.dp),
                                contentPadding = PaddingValues(horizontal = 32.dp)
                        ) {
                            Text(
                                    "Submit",
                                    style =
                                            androidx.compose.ui.text.TextStyle(
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                            )
                            )
                        }
                    }
                }
            }
    ) { innerPadding ->
        // Main Form Content Node (p-4 space-y-6)
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(innerPadding)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp), // p-4
                verticalArrangement = Arrangement.spacedBy(24.dp) // space-y-6
        ) {

            // Section 1: General Information
            Column {
                Text(
                        text = "General Information".uppercase(),
                        style =
                                androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.5.sp,
                                        color = MaterialTheme.colorScheme.primary
                                ),
                        modifier = Modifier.padding(bottom = 16.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ValidatedTextField(
                            label = "Project ID",
                            value = uiState.projectId,
                            onValueChange = {},
                            errorText = null,
                            isRequired = false,
                            readOnly = true,
                            description = "e.g. PRJ-2024-001"
                    )

                    ValidatedTextField(
                            label = "Project Name",
                            value = uiState.projectName,
                            onValueChange = { viewModel.updateField("projectName", it) },
                            errorText = uiState.errors["projectName"],
                            isRequired = true,
                            description = "Enter project name"
                    )

                    ValidatedTextField(
                            label = "Description",
                            value = uiState.description,
                            onValueChange = { viewModel.updateField("description", it) },
                            errorText = uiState.errors["description"],
                            isRequired = false,
                            singleLine = false,
                            modifier = Modifier.height(100.dp),
                            description = "Briefly describe the project scope and expenses..."
                    )
                }
            }

            // Section 2: Dates & Management
            Column {
                Text(
                        text = "Dates & Management".uppercase(),
                        style =
                                androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.5.sp,
                                        color = MaterialTheme.colorScheme.primary
                                ),
                        modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DatePickerField(
                            label = "Start Date",
                            value = uiState.startDate,
                            onValueChange = { viewModel.updateField("startDate", it) },
                            errorText = uiState.errors["startDate"],
                            isRequired = true,
                            modifier = Modifier.weight(1f),
                            description = "MM/DD/YYYY"
                    )

                    DatePickerField(
                            label = "End Date",
                            value = uiState.endDate,
                            onValueChange = { viewModel.updateField("endDate", it) },
                            errorText = uiState.errors["endDate"],
                            isRequired = true,
                            modifier = Modifier.weight(1f),
                            description = "MM/DD/YYYY"
                    )
                }

                ValidatedTextField(
                        label = "Manager Name",
                        value = uiState.manager,
                        onValueChange = { viewModel.updateField("manager", it) },
                        errorText = uiState.errors["manager"],
                        isRequired = true,
                        description = "Assign a project lead"
                )
            }

            // Section 3: Financials & Status
            Column {
                Text(
                        text = "Financials & Status".uppercase(),
                        style =
                                androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.5.sp,
                                        color = MaterialTheme.colorScheme.primary
                                ),
                        modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Dropdown
                    var expanded by remember { mutableStateOf(false) }
                    val statuses = listOf("Active", "Completed", "On Hold")
                    Column(modifier = Modifier.weight(1f)) {
                        ValidatedTextField(
                                label = "Status",
                                value = uiState.status,
                                onValueChange = {},
                                errorText = null,
                                isRequired = false,
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                            Icons.Default.ArrowDropDown,
                                            "Select Status",
                                            modifier = Modifier.clickable { expanded = true }
                                    )
                                }
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            statuses.forEach { status ->
                                DropdownMenuItem(
                                        text = { Text(status) },
                                        onClick = {
                                            viewModel.updateField("status", status)
                                            expanded = false
                                        }
                                )
                            }
                        }
                    }

                    // Budget
                    ValidatedTextField(
                            label = "Budget",
                            value = uiState.budget,
                            onValueChange = { viewModel.updateField("budget", it) },
                            errorText = uiState.errors["budget"],
                            isRequired = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            description = "0.00"
                    )
                }
            }

            // Section 4: Additional Details (Optional)
            Column(modifier = Modifier.padding(bottom = 0.dp)) {
                Text(
                        text = "Additional Details (Optional)".uppercase(),
                        style =
                                androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.5.sp,
                                        color = MaterialTheme.colorScheme.primary
                                ),
                        modifier = Modifier.padding(bottom = 16.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ValidatedTextField(
                            label = "Client / Department",
                            value = uiState.clientInfo,
                            onValueChange = { viewModel.updateField("clientInfo", it) },
                            errorText = null,
                            isRequired = false,
                            description = "Internal or External client"
                    )

                    // Priority
                    Column {
                        Text(
                                text = "Priority Level",
                                style =
                                        androidx.compose.ui.text.TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                        ),
                                modifier =
                                        Modifier.padding(
                                                bottom = 6.dp,
                                                start = 4.dp
                                        )
                        )
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val priorities = listOf("Low", "Medium", "High")
                            priorities.forEach { priority ->
                                val isSelected = uiState.priority == priority
                                Button(
                                        onClick = { viewModel.updateField("priority", priority) },
                                        modifier =
                                                Modifier.weight(1f)
                                                        .height(40.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        border =
                                                androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (isSelected)
                                                                MaterialTheme.colorScheme.primary
                                                        else
                                                                MaterialTheme.colorScheme.outlineVariant
                                                ),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor =
                                                                if (isSelected)
                                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                                else
                                                                        MaterialTheme.colorScheme.surface
                                                ),
                                        contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                            priority,
                                            style =
                                                    androidx.compose.ui.text.TextStyle(
                                                            fontSize = 14.sp,
                                                            fontWeight =
                                                                    if (isSelected)
                                                                            FontWeight.Medium
                                                                    else FontWeight.Normal,
                                                            color =
                                                                    if (isSelected)
                                                                            MaterialTheme.colorScheme.primary
                                                                    else
                                                                            MaterialTheme.colorScheme.onSurface
                                                    )
                                    )
                                }
                            }
                        }
                    }

                    ValidatedTextField(
                            label = "Special Requirements",
                            value = uiState.specialRequirements,
                            onValueChange = { viewModel.updateField("specialRequirements", it) },
                            errorText = null,
                            isRequired = false,
                            singleLine = false,
                            modifier = Modifier.height(100.dp)
                    )
                }
            }
        }
    }
}

// Reusable Components Node Mapping

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidatedTextField(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        errorText: String?,
        isRequired: Boolean,
        modifier: Modifier = Modifier,
        description: String? = null,
        singleLine: Boolean = true,
        readOnly: Boolean = false,
        trailingIcon: @Composable (() -> Unit)? = null,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                    text = label,
                    style =
                            androidx.compose.ui.text.TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                            ),
                    modifier = Modifier.padding(horizontal = 4.dp)
            )
            if (isRequired) {
                Text(
                        text = " *",
                        style =
                                androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.error
                                )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle =
                        androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                        ),
                modifier =
                        Modifier.fillMaxWidth()
                                .height(56.dp),
                isError = errorText != null,
                singleLine = singleLine,
                readOnly = readOnly,
                trailingIcon = trailingIcon,
                keyboardOptions = keyboardOptions,
                shape = RoundedCornerShape(8.dp),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                errorContainerColor = MaterialTheme.colorScheme.surface
                        )
        )

        if (errorText != null) {
            Text(
                    text = errorText,
                    style = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        } else if (description != null) {
            Text(
                    text = description,
                    style = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp,
                            color = AppTheme.extended.textSecondary
                    ),
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        errorText: String?,
        isRequired: Boolean,
        modifier: Modifier = Modifier,
        description: String? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDialog) {
        DatePickerDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val formattedDate =
                                            java.text.SimpleDateFormat(
                                                            "dd/MM/yyyy",
                                                            java.util.Locale.getDefault()
                                                    )
                                                    .format(java.util.Date(millis))
                                    onValueChange(formattedDate)
                                }
                                showDialog = false
                            }
                    ) { Text("OK", color = MaterialTheme.colorScheme.primary) }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                }
        ) { DatePicker(state = datePickerState) }
    }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                    text = label,
                    style =
                            androidx.compose.ui.text.TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                            ),
                    modifier = Modifier.padding(horizontal = 4.dp)
            )
            if (isRequired) {
                Text(
                        text = " *",
                        style =
                                androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.error
                                )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                    value = value,
                    onValueChange = {},
                    textStyle =
                            androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                            ),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    isError = errorText != null,
                    readOnly = true,
                    placeholder = {
                        Text("MM/DD/YYYY", color = AppTheme.extended.textTertiary)
                    },
                    trailingIcon = {
                        Icon(
                                Icons.Default.DateRange,
                                "Select Date",
                                tint = AppTheme.extended.textSecondary
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors =
                            OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    errorContainerColor = MaterialTheme.colorScheme.surface
                            )
            )
            Box(
                    Modifier.matchParentSize()
                            .clickable(
                                    onClick = { showDialog = true },
                                    indication = null,
                                    interactionSource =
                                            remember {
                                                androidx.compose.foundation.interaction
                                                        .MutableInteractionSource()
                                            }
                            )
            )
        }

        if (errorText != null) {
            Text(
                    text = errorText,
                    style = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        } else if (description != null) {
            Text(
                    text = description,
                    style = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp,
                            color = AppTheme.extended.textSecondary
                    ),
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        }
    }
}
