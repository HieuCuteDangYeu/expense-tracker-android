package com.example.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.expensetracker.ui.components.DeleteConfirmationDialog
import com.example.expensetracker.ui.components.FullScreenLoadingIndicator
import com.example.expensetracker.ui.components.StatusBadge
import com.example.expensetracker.ui.components.getIconAndColorsForType
import com.example.expensetracker.ui.theme.AppTheme
import com.example.expensetracker.util.FormatUtils
import com.example.expensetracker.viewmodel.ExpenseDetailsViewModel
import com.example.expensetracker.viewmodel.ExpenseViewModel

@Composable
fun ExpenseDetailsScreen(
    viewModel: ExpenseDetailsViewModel,
    sharedViewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit,
    showDeleteDialog: Boolean = false,
    onDismissDeleteDialog: () -> Unit = {},
    triggerEdit: Boolean = false,
    onEditConsumed: () -> Unit = {}
) {
    val expense by viewModel.expense.collectAsState()
    var internalShowDelete by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }

    // Sync external trigger to internal state
    androidx.compose.runtime.LaunchedEffect(showDeleteDialog) {
        if (showDeleteDialog) internalShowDelete = true
    }

    androidx.compose.runtime.LaunchedEffect(triggerEdit, expense) {
        if (triggerEdit && expense != null) {
            sharedViewModel.loadExpenseForEdit(expense!!)
            showEditSheet = true
            onEditConsumed()
        }
    }

    if (internalShowDelete) {
        DeleteConfirmationDialog(
            title = "Delete Expense",
            message = "Are you sure you want to permanently delete this expense? This action cannot be undone.",
            onConfirm = {
                viewModel.deleteExpense { onNavigateBack() }
                internalShowDelete = false
                onDismissDeleteDialog()
            },
            onDismiss = {
                internalShowDelete = false
                onDismissDeleteDialog()
            }
        )
    }

    if (expense != null) {
        val exp = expense!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Amount Header ──────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Category icon pill
                    val (icon, bgColor, tintColor) = getIconAndColorsForType(exp.type)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(bgColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = exp.type,
                            tint = tintColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount
                    Text(
                        text = FormatUtils.formatCurrency(exp.amount, exp.currency),
                        style = TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status badge
                    StatusBadge(
                        status = exp.paymentStatus,
                        fontSize = 12.sp,
                        horizontalPadding = 16.dp,
                        verticalPadding = 4.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Details Card ──────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    DetailRow(label = "Category", value = exp.type)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    DetailRow(label = "Date", value = exp.date)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    if (!exp.location.isNullOrBlank()) {
                        DetailRow(label = "Location", value = exp.location)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    DetailRow(label = "Payment Method", value = "Paid via ${exp.paymentMethod}")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    DetailRow(label = "Claimant", value = exp.claimant)
                }
            }

            // ── Description Section ──────────────────────
            if (!exp.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Description",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = exp.description,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = AppTheme.extended.textSecondary,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }
            }

            // ── Receipt Section ──────────────────────────
            if (!exp.receiptUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Receipt",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val receiptUrl = exp.receiptUrl
                        val imageModel: Any = if (receiptUrl.startsWith("http")) {
                            receiptUrl
                        } else {
                            java.io.File(receiptUrl)
                        }
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Receipt image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showEditSheet) {
            AddExpenseBottomSheet(
                viewModel = sharedViewModel,
                onDismiss = { showEditSheet = false }
            )
        }
    } else {
        FullScreenLoadingIndicator()
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                color = AppTheme.extended.textSecondary
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
