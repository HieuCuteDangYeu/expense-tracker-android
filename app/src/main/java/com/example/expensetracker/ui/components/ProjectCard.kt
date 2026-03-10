package com.example.expensetracker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.ProjectWithExpenses
import com.example.expensetracker.ui.theme.AppTheme
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProjectCard(
        projectWithExpenses: ProjectWithExpenses,
        onClick: () -> Unit,
        onEdit: () -> Unit = {},
        onDelete: () -> Unit = {},
        modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val project = projectWithExpenses.project
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    format.currency = Currency.getInstance("USD")
    format.maximumFractionDigits = 0

    // Calculate real totalSpent from ProjectWithExpenses relation
    val totalSpent = projectWithExpenses.expenses.sumOf { it.amount }
    val budgetPercentage = if (project.budget > 0) (totalSpent / project.budget) else 0.0
    val budgetPercentageInt = (budgetPercentage * 100).toInt()

    val (statusBgColor, statusTextColor) =
            when (project.status.lowercase()) {
                "active" -> AppTheme.extended.successBg to AppTheme.extended.successText
                "on hold" -> AppTheme.extended.warningBg to AppTheme.extended.warningText
                "completed" ->
                        MaterialTheme.colorScheme.surfaceVariant to
                                MaterialTheme.colorScheme.onSurfaceVariant
                else -> AppTheme.extended.infoBg to AppTheme.extended.infoText
            }

    var showMenuOverlay by remember { mutableStateOf(false) }
    val dismissState =
            rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            onDelete()
                            true
                        } else {
                            false
                        }
                    }
            )

    SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                val color = Color(0xFFEF4444) // bg-red-500
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(180.dp) // Exact matching card height for bg
                                        .background(color, RoundedCornerShape(8.dp))
                                        .padding(end = 24.dp),
                        contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                    )
                }
            }
    ) {
        Box(modifier = modifier) {
            Surface(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .combinedClickable(
                                            onClick = {
                                                if (showMenuOverlay) showMenuOverlay = false
                                                else onClick()
                                            },
                                            onLongClick = { showMenuOverlay = true }
                                    ),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                    text = project.projectName,
                                    style =
                                            androidx.compose.ui.text.TextStyle(
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                            )
                            )
                            Text(
                                    text = "ID: ${project.formattedId}",
                                    style =
                                            androidx.compose.ui.text.TextStyle(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    letterSpacing = (-0.025).sp, // tracking-tight
                                                    color = AppTheme.extended.textTertiary
                                            ),
                                    modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Box(
                                modifier =
                                        Modifier.background(statusBgColor, RoundedCornerShape(50))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                    text = project.status.uppercase(),
                                    style =
                                            androidx.compose.ui.text.TextStyle(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = statusTextColor
                                            )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                                text = "Budget Used: $budgetPercentageInt%",
                                style =
                                        androidx.compose.ui.text.TextStyle(
                                                fontSize = 12.sp,
                                                color = AppTheme.extended.textSecondary
                                        )
                        )
                        Text(
                                text =
                                        "${format.format(totalSpent)} / ${format.format(project.budget)}",
                                style =
                                        androidx.compose.ui.text.TextStyle(
                                                fontSize = 12.sp,
                                                color = AppTheme.extended.textSecondary
                                        )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress Bar
                    val progressBarColor =
                            if (budgetPercentage > 0.8) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                    Box(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(percent = 50))
                                            .background(
                                                    MaterialTheme.colorScheme.surfaceVariant
                                            )
                    ) {
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth(
                                                        (budgetPercentage)
                                                                .toFloat()
                                                                .coerceIn(0f, 1f)
                                                )
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(percent = 50))
                                                .background(progressBarColor)
                        )
                    }
                }

                if (showMenuOverlay) {
                    Box(
                            modifier =
                                    Modifier.matchParentSize()
                                            .background(
                                                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f),
                                                    RoundedCornerShape(8.dp)
                                            )
                                            .clickable(
                                                    interactionSource =
                                                            remember {
                                                                androidx.compose.foundation
                                                                        .interaction
                                                                        .MutableInteractionSource()
                                                            },
                                                    indication = null,
                                                    onClick = { showMenuOverlay = false }
                                            ),
                            contentAlignment = Alignment.Center
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            IconButton(
                                    onClick = {
                                        showMenuOverlay = false
                                        onEdit()
                                    },
                                    modifier =
                                            Modifier.size(48.dp)
                                                    .background(
                                                            MaterialTheme.colorScheme.surface,
                                                            androidx.compose.foundation.shape
                                                                    .CircleShape
                                                    )
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Project",
                                        tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                    onClick = { 
                                        showMenuOverlay = false
                                        val shareText = generateShareText(projectWithExpenses)
                                        shareProject(context, shareText)
                                    },
                                    modifier =
                                            Modifier.size(48.dp)
                                                    .background(
                                                            MaterialTheme.colorScheme.surface,
                                                            androidx.compose.foundation.shape
                                                                    .CircleShape
                                                    )
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Project",
                                        tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun generateShareText(projectWithExpenses: ProjectWithExpenses): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    format.currency = Currency.getInstance("USD")
    format.maximumFractionDigits = 0

    val project = projectWithExpenses.project
    val totalSpent = projectWithExpenses.expenses.sumOf { it.amount }

    val sb = StringBuilder()
    sb.appendLine("Project: ${project.projectName}")
    sb.appendLine("Budget: ${format.format(project.budget)}")
    sb.appendLine("Total Spent: ${format.format(totalSpent)}")
    
    if (projectWithExpenses.expenses.isNotEmpty()) {
        sb.appendLine("\nExpenses:")
        projectWithExpenses.expenses.forEach { expense ->
            val date = expense.date.takeIf { it.isNotBlank() } ?: "No Date"
            val location = expense.location.takeIf { it?.isNotBlank() == true } ?: "No Location"
            sb.appendLine("- [$date] [$location] ${format.format(expense.amount)}")
        }
    }

    return sb.toString()
}

fun shareProject(context: Context, shareText: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Project Summary")
    context.startActivity(shareIntent)
}
