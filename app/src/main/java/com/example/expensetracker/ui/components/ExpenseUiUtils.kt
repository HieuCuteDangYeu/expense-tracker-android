package com.example.expensetracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

fun getIconAndColorsForType(type: String): Triple<ImageVector, Color, Color> = when (type.lowercase()) {
    "travel" -> Triple(Icons.Default.Person, Color(0xFFDBEAFE), Color(0xFF2563EB))       // blue-100, blue-600
    "equipment" -> Triple(Icons.Default.Build, Color(0xFFFFEDD5), Color(0xFFEA580C))      // orange-100, orange-600
    "materials" -> Triple(Icons.Default.ShoppingCart, Color(0xFFF3E8FF), Color(0xFF9333EA)) // purple-100, purple-600
    "services" -> Triple(Icons.AutoMirrored.Filled.List, Color(0xFFE0E7FF), Color(0xFF4F46E5)) // indigo-100, indigo-600
    "software" -> Triple(Icons.Default.Settings, Color(0xFFDCFCE7), Color(0xFF16A34A))    // green-100, green-600
    "labour" -> Triple(Icons.Default.Star, Color(0xFFFCE7F3), Color(0xFFDB2777))          // pink-100, pink-600
    "utilities" -> Triple(Icons.Default.Warning, Color(0xFFFEF9C3), Color(0xFFCA8A04))    // yellow-100, yellow-600
    else -> Triple(Icons.Default.Info, Color(0xFFF1F5F9), Color(0xFF475569))              // slate-100, slate-600 (Misc)
}

fun getColorsForStatus(status: String): Pair<Color, Color> = when (status.lowercase()) {
    "paid" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))       // green-100, green-700
    "pending" -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))    // amber-100, amber-700
    "reimbursed" -> Pair(Color(0xFFDBEAFE), Color(0xFF1D4ED8)) // blue-100, blue-700
    else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))         // slate
}
