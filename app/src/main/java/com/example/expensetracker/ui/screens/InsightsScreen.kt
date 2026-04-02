package com.example.expensetracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.ui.theme.AppTheme
import com.example.expensetracker.ui.components.EmptyStateMessage
import com.example.expensetracker.util.FormatUtils
import com.example.expensetracker.viewmodel.InsightsUiState
import com.example.expensetracker.viewmodel.InsightsViewModel

// Category colors for the breakdown bars and donut chart
// These are intentionally kept as fixed brand colors for data visualization
// and do not change between light/dark mode.
private val categoryColors = listOf(
    Color(0xFF2F3E46),  // Slate primary
    Color(0xFF3B82F6),  // Blue-500
    Color(0xFF22C55E),  // Green-500
    Color(0xFFF59E0B),  // Amber-500
    Color(0xFFEF4444),  // Red-500
    Color(0xFF8B5CF6),  // Violet-500
    Color(0xFF06B6D4),  // Cyan-500
    Color(0xFFF97316),  // Orange-500
)

/** Helper to format currency amounts consistently throughout Insights. */
private fun formatAmount(amount: Double): String = FormatUtils.formatCurrency(amount)

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (uiState.expensesByType.isEmpty()) {
            EmptyStateMessage(
                title = "No Expenses Yet",
                description = "Add expenses to your projects to see spending insights and analytics here.",
                icon = Icons.Default.Info
            )
        } else {
            // ── Summary Cards Row ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    label = "Most Expensive",
                    value = uiState.topExpenseType?.expenseType ?: "—",
                    subtitle = if (uiState.topExpenseType != null)
                        formatAmount(uiState.topExpenseType!!.totalAmount) else "—",
                    accentColor = MaterialTheme.colorScheme.primary
                )

                val budgetStatusLabel = if (uiState.isUnderBudget) "Under budget" else "Over budget"
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    label = "Budget Used",
                    value = "${uiState.budgetUsedPercent}%",
                    subtitle = budgetStatusLabel,
                    accentColor = if (uiState.isUnderBudget) AppTheme.extended.successText else MaterialTheme.colorScheme.error,
                    badgeBg = if (uiState.isUnderBudget) AppTheme.extended.successBg else AppTheme.extended.errorBg,
                    badgeText = if (uiState.isUnderBudget) AppTheme.extended.successText else AppTheme.extended.errorText
                )
            }

            // ── Donut Chart ──────────────────────────────────────────────
            DonutChartCard(uiState = uiState)

            // ── Expense Breakdown (Progress Bars) ────────────────────────
            ExpenseBreakdownCard(uiState = uiState)

            // ── Spending Trend Card ──────────────────────────────────────
            SpendingTrendCard(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Summary Card ─────────────────────────────────────────────────────
@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    badgeBg: Color = Color.Transparent,
    badgeText: Color = AppTheme.extended.textSecondary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.extended.textSecondary,
                    letterSpacing = 0.3.sp
                )
            )
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (badgeBg != Color.Transparent) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = subtitle,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeText
                        )
                    )
                }
            } else {
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                )
            }
        }
    }
}

// ── Donut Chart Card ─────────────────────────────────────────────────
@Composable
private fun DonutChartCard(
    uiState: InsightsUiState
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SPENDING OVERVIEW",
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.extended.textSecondary,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Donut Chart with center text
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                val totalExpenses = uiState.totalExpenses
                val categories = uiState.expensesByType

                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeWidth = 28.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    var startAngle = -90f

                    categories.forEachIndexed { index, category ->
                        val sweepAngle = if (totalExpenses > 0) {
                            (category.totalAmount / totalExpenses * 360f).toFloat()
                        } else 0f

                        drawArc(
                            color = categoryColors[index % categoryColors.size],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += sweepAngle
                    }
                }

                // Center text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = AppTheme.extended.textSecondary
                        )
                    )
                    Text(
                        text = formatAmount(uiState.totalExpenses),
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.expensesByType.forEachIndexed { index, category ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(categoryColors[index % categoryColors.size])
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category.expenseType,
                            modifier = Modifier.weight(1f),
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = formatAmount(category.totalAmount),
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}

// ── Expense Breakdown Card (Progress Bars) ───────────────────────────
@Composable
private fun ExpenseBreakdownCard(
    uiState: InsightsUiState
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "EXPENSE BREAKDOWN",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.extended.textSecondary,
                    letterSpacing = 0.5.sp
                )
            )

            uiState.expensesByType.forEachIndexed { index, category ->
                val percentage = if (uiState.totalExpenses > 0) {
                    (category.totalAmount / uiState.totalExpenses).toFloat()
                } else 0f

                val animatedProgress by animateFloatAsState(
                    targetValue = percentage,
                    animationSpec = tween(durationMillis = 800),
                    label = "progress_$index"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(categoryColors[index % categoryColors.size])
                            )
                            Text(
                                text = category.expenseType,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${(percentage * 100).toInt()}%",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.extended.textSecondary
                                )
                            )
                            Text(
                                text = formatAmount(category.totalAmount),
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = categoryColors[index % categoryColors.size],
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

// ── Spending Trend Card ──────────────────────────────────────────────
@Composable
private fun SpendingTrendCard(
    uiState: InsightsUiState
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AppTheme.extended.infoBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.extended.infoBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = AppTheme.extended.infoText,
                modifier = Modifier.size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Spending Trend",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.extended.infoText
                    )
                )
                val trendText = if (uiState.isUnderBudget) {
                    "You are currently ${100 - uiState.budgetUsedPercent}% under your total budget of ${formatAmount(uiState.totalBudget)}."
                } else {
                    "You've exceeded your total budget of ${formatAmount(uiState.totalBudget)} by ${formatAmount(uiState.totalExpenses - uiState.totalBudget)}."
                }
                Text(
                    text = trendText,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = AppTheme.extended.infoText.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

