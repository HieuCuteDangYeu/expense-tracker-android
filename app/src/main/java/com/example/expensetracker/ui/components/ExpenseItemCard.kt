package com.example.expensetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.ExpenseEntity
import com.example.expensetracker.ui.theme.AppTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ExpenseItemCard(expense: ExpenseEntity) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp), // p-3 -> 12.dp
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, bgColor, tintColor) = getIconAndColorsForType(expense.type)
            
            Box(
                modifier = Modifier
                    .size(40.dp) // size-10 -> 40.dp
                    .background(bgColor, RoundedCornerShape(8.dp)), // rounded-lg -> 8.dp
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = expense.type,
                    tint = tintColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp)) // gap-4 -> 16.dp
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = expense.description ?: expense.type,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatCurrency(expense.amount, expense.currency),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp)) // mt-0.5 -> 2.dp
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${expense.date} • ${expense.type}",
                        style = TextStyle(
                            fontSize = 12.sp, // text-xs
                            color = AppTheme.extended.textSecondary
                        )
                    )
                    
                    val (statusBg, statusText) = getColorsForStatus(expense.paymentStatus)
                    Box(
                        modifier = Modifier
                            .background(statusBg, RoundedCornerShape(percent = 50)) // rounded-full
                            .padding(horizontal = 8.dp, vertical = 2.dp) // px-2 py-0.5
                    ) {
                        Text(
                            text = expense.paymentStatus.uppercase(),
                            style = TextStyle(
                                fontSize = 10.sp, // text-[10px]
                                fontWeight = FontWeight.Bold, // font-bold
                                color = statusText
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun getIconAndColorsForType(type: String) = when (type.lowercase()) {
    "travel" -> Triple(Icons.Default.Person, Color(0xFFDBEAFE), Color(0xFF2563EB)) // blue-100, blue-600
    "equipment" -> Triple(Icons.Default.Build, Color(0xFFFFEDD5), Color(0xFFEA580C)) // orange-100, orange-600
    "materials" -> Triple(Icons.Default.ShoppingCart, Color(0xFFF3E8FF), Color(0xFF9333EA)) // purple-100, purple-600
    "services" -> Triple(Icons.AutoMirrored.Filled.List, Color(0xFFE0E7FF), Color(0xFF4F46E5)) // indigo-100, indigo-600
    "software" -> Triple(Icons.Default.Settings, Color(0xFFDCFCE7), Color(0xFF16A34A)) // green-100, green-600
    "labour" -> Triple(Icons.Default.Star, Color(0xFFFCE7F3), Color(0xFFDB2777)) // pink-100, pink-600
    "utilities" -> Triple(Icons.Default.Warning, Color(0xFFFEF9C3), Color(0xFFCA8A04)) // yellow-100, yellow-600
    else -> Triple(Icons.Default.Info, Color(0xFFF1F5F9), Color(0xFF475569)) // slate-100, slate-600 (Misc)
}

private fun getColorsForStatus(status: String) = when (status.lowercase()) {
    "paid" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D)) // green-100, green-700
    "pending" -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309)) // amber-100, amber-700
    "reimbursed" -> Pair(Color(0xFFDBEAFE), Color(0xFF1D4ED8)) // blue-100, blue-700
    else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569)) // slate
}

private fun formatCurrency(amount: Double, currency: String): String {
    val symbol = when {
        currency.contains("€") -> "€"
        currency.contains("£") -> "£"
        else -> "$"
    }
    return "$symbol${String.format(Locale.US, "%,.2f", amount)}"
}
