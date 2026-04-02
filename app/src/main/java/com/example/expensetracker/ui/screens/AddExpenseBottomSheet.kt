package com.example.expensetracker.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.expensetracker.ui.components.AppDatePickerField
import com.example.expensetracker.ui.theme.AppTheme
import com.example.expensetracker.util.LocationHelper
import com.example.expensetracker.viewmodel.ExpenseViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseBottomSheet(
    viewModel: ExpenseViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiState by viewModel.formState.collectAsState()
    val isLocationLoading by viewModel.isLocationLoading.collectAsState()
    val showGpsDialog by viewModel.showGpsDialog.collectAsState()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val locationHelper = remember { LocationHelper(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                viewModel.setLocationLoading(false)
                viewModel.setShowGpsDialog(true)
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                try {
                    viewModel.setLocationLoading(true)
                    val address = locationHelper.getCurrentLocationAddress()
                    if (address != null) {
                        viewModel.updateField("location", address)
                    }
                } finally {
                    viewModel.setLocationLoading(false)
                }
            }
        }
    }

    if (showGpsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissGpsDialog() },
            title = { Text("Location Disabled") },
            text = { Text("Your device's location services are turned off. Please enable GPS to automatically detect your address.") },
            confirmButton = {
                TextButton(onClick = {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(intent)
                    viewModel.dismissGpsDialog()
                }) {
                    Text("Turn On Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissGpsDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Camera: Create temp file, use FileProvider URI, then launch TakePicture
    var cameraImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            val localPath = viewModel.cacheImageLocally(context, cameraImageUri!!)
            if (localPath != null) {
                viewModel.setReceiptUri(localPath)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val localPath = viewModel.cacheImageLocally(context, uri)
            if (localPath != null) {
                viewModel.setReceiptUri(localPath)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val imageFile = File(
                context.cacheDir,
                "images/receipt_${System.currentTimeMillis()}.jpg"
            ).also { it.parentFile?.mkdirs() }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetForm()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: "Add New Expense" + "NEW ENTRY" badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add New Expense",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NEW ENTRY",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = AppTheme.extended.textTertiary,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row 1: Date + Amount (2-column grid, gap-4 = 16.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppDatePickerField(
                    label = "DATE",
                    value = uiState.date,
                    onDateSelected = { it?.let { date -> viewModel.updateField("date", date) } },
                    errorText = uiState.errors["date"],
                    modifier = Modifier.weight(1f)
                )
                ExpenseAmountField(
                    value = uiState.amount,
                    onValueChange = { viewModel.updateField("amount", it) },
                    errorText = uiState.errors["amount"],
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row 2: Category + Currency (2-column grid, gap-4 = 16.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StitchDropdownField(
                    label = "CATEGORY",
                    options = listOf("Materials", "Travel", "Equipment", "Services", "Software", "Labour", "Utilities", "Misc"),
                    selectedOption = uiState.category,
                    onOptionSelected = { viewModel.updateField("category", it) },
                    modifier = Modifier.weight(1f)
                )
                StitchDropdownField(
                    label = "CURRENCY",
                    options = listOf("USD ($)", "EUR (€)", "GBP (£)"),
                    selectedOption = uiState.currency,
                    onOptionSelected = { viewModel.updateField("currency", it) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method - Segmented Button Bar (grid-cols-4)
            StitchSegmentedButtonBar(
                label = "PAYMENT METHOD",
                options = listOf("Cash", "Card", "Transfer", "Cheque"),
                selectedOption = uiState.paymentMethod,
                onOptionSelected = { viewModel.updateField("paymentMethod", it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Location text input (Auto-Location Integration)
            ExpenseLocationField(
                label = "LOCATION",
                value = uiState.location,
                onValueChange = { viewModel.updateField("location", it) },
                placeholder = "City, Street",
                errorText = uiState.errors["location"],
                isLoading = isLocationLoading,
                onGpsClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description text input
            StitchTextField(
                label = "DESCRIPTION (OPTIONAL)",
                value = uiState.description,
                onValueChange = { viewModel.updateField("description", it) },
                placeholder = "Additional details...",
                errorText = uiState.errors["description"]
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Attach Receipt Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ATTACH RECEIPT",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.extended.textSecondary
                    )
                )

                if (uiState.receiptUri != null) {
                    // Show thumbnail with X clear button
                    Box(modifier = Modifier.size(80.dp)) {
                        AsyncImage(
                            model = if (uiState.receiptUri!!.startsWith("http")) uiState.receiptUri else java.io.File(uiState.receiptUri!!),
                            contentDescription = "Receipt thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        IconButton(
                            onClick = { viewModel.setReceiptUri(null) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove receipt",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    // Show Camera and Gallery buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Camera",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Camera", style = TextStyle(fontSize = 13.sp))
                        }
                        androidx.compose.material3.OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Gallery", style = TextStyle(fontSize = 13.sp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Claimant text input
            StitchTextField(
                label = "CLAIMANT",
                value = uiState.claimant,
                onValueChange = { viewModel.updateField("claimant", it) },
                placeholder = "e.g. John Doe",
                errorText = uiState.errors["claimant"]
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status - Segmented Button Bar (grid-cols-3)
            StitchSegmentedButtonBar(
                label = "STATUS",
                options = listOf("Paid", "Pending", "Reimbursed"),
                selectedOption = uiState.status,
                onOptionSelected = { viewModel.updateField("status", it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        viewModel.resetForm()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = "Cancel",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Button(
                    onClick = {
                        if (viewModel.saveExpense()) {
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Save Entry",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


// ── Amount Field with $ prefix ─────────────────────────────────────────────────
@Composable
private fun ExpenseAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    errorText: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "AMOUNT",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.extended.textSecondary
            )
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("0.00", style = TextStyle(fontSize = 14.sp, color = AppTheme.extended.textTertiary))
            },
            leadingIcon = {
                Text(
                    "$",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = AppTheme.extended.textTertiary
                    )
                )
            },
            textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = errorText != null
        )
        if (errorText != null) {
            Text(
                text = errorText,
                style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.error),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ── Segmented Button Bar ───────────────────────────────────────────────────────
@Composable
private fun StitchSegmentedButtonBar(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.extended.textSecondary
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            options.forEach { option ->
                val isSelected = selectedOption == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (isSelected) {
                                Modifier
                                    .shadow(2.dp, RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                            } else {
                                Modifier
                            }
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onOptionSelected(option) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else AppTheme.extended.textSecondary
                        )
                    )
                }
            }
        }
    }
}

// ── Dropdown Field ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StitchDropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.extended.textSecondary
            )
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, style = TextStyle(fontSize = 14.sp)) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ── Text Input Field ──────────────────────────────────────────────────────────
@Composable
private fun StitchTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    errorText: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.extended.textSecondary
            )
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder, style = TextStyle(fontSize = 14.sp, color = AppTheme.extended.textTertiary))
            },
            textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
            singleLine = true,
            isError = errorText != null
        )
        if (errorText != null) {
            Text(
                text = errorText,
                style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.error),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ── Location Input Field (with GPS Auto-Detect) ──────────────────────────────
@Composable
fun ExpenseLocationField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    errorText: String?,
    isLoading: Boolean = false,
    onGpsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.extended.textSecondary
            )
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            placeholder = {
                Text(placeholder, style = TextStyle(fontSize = 14.sp, color = AppTheme.extended.textTertiary))
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    IconButton(onClick = onGpsClick, enabled = !isLoading) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Auto-detect location",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                errorBorderColor = MaterialTheme.colorScheme.error,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = AppTheme.extended.textTertiary,
                disabledPlaceholderColor = AppTheme.extended.textTertiary
            ),
            singleLine = true,
            isError = errorText != null
        )
        if (errorText != null) {
            Text(
                text = errorText,
                style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.error),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
