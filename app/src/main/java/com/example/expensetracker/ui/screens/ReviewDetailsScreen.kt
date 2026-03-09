package com.example.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.ui.theme.AppTheme
import com.example.expensetracker.viewmodel.ProjectFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDetailsBottomSheet(
    viewModel: ProjectFormViewModel,
    onDismiss: () -> Unit,
    onConfirmAndSave: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(6.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                )
            }
        }
    ) {
        Column {
            // Header
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Review Details",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Text(
                    "Please verify the project information before final confirmation.",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        color = AppTheme.extended.textSecondary
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Key-Value List
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ReviewRow(label = "Project ID", value = uiState.projectId.ifBlank { "N/A" })
                ReviewRow(label = "Project Name", value = uiState.projectName.ifBlank { "N/A" })
                ReviewRow(label = "Manager", value = uiState.manager.ifBlank { "N/A" })
                ReviewRow(
                    label = "Total Budget",
                    value = if (uiState.budget.isBlank()) "N/A" else "$${uiState.budget}",
                    isValueBold = true,
                    valueColor = MaterialTheme.colorScheme.primary
                )
                ReviewRow(label = "Start Date", value = uiState.startDate.ifBlank { "N/A" })
                ReviewRow(label = "End Date", value = uiState.endDate.ifBlank { "N/A" })
                ReviewRow(label = "Priority", value = uiState.priority.ifBlank { "N/A" })
                if (uiState.description.isNotBlank()) {
                    ReviewRow(label = "Description", value = uiState.description)
                }
                if (uiState.specialRequirements.isNotBlank()) {
                    ReviewRow(label = "Special Requirements", value = uiState.specialRequirements)
                }
                if (uiState.clientInfo.isNotBlank()) {
                    ReviewRow(label = "Client Information", value = uiState.clientInfo)
                }
            }

            // Modal Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.weight(1f).height(44.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Edit", style = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = { viewModel.saveProject(onSuccess = onConfirmAndSave) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).height(44.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Confirm", style = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ReviewRow(
    label: String,
    value: String,
    isValueBold: Boolean = false,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp)
                .background(androidx.compose.ui.graphics.Color.Transparent),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.5).sp,
                    color = AppTheme.extended.textSecondary
                )
            )
            Text(
                text = value,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    fontWeight = if (isValueBold) FontWeight.Bold else FontWeight.SemiBold,
                    color = valueColor
                ),
                modifier = Modifier.padding(start = 16.dp),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
    }
}
