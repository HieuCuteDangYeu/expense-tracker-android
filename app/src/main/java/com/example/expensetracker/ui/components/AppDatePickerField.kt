package com.example.expensetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.ui.theme.AppTheme
import com.example.expensetracker.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerField(
    label: String,
    value: String?,
    onDateSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    isRequired: Boolean = false,
    description: String? = null,
    placeholder: String = "Select date",
    showClearButton: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(DateUtils.formatMillisToDate(millis))
                    }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (showClearButton) onDateSelected(null)
                    showDialog = false
                }) { Text(if (showClearButton) "Clear" else "Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                if (isRequired) {
                    Text(
                        text = " *",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                isError = errorText != null,
                placeholder = {
                    Text(placeholder, color = AppTheme.extended.textTertiary)
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Select Date",
                        tint = AppTheme.extended.textSecondary
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorContainerColor = MaterialTheme.colorScheme.surface
                ),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Box(
                Modifier
                    .matchParentSize()
                    .clickable(
                        onClick = { showDialog = true },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )
        }

        if (errorText != null) {
            Text(
                text = errorText,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        } else if (description != null) {
            Text(
                text = description,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = AppTheme.extended.textSecondary
                ),
                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        }
    }
}
