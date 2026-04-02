package com.example.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 10.sp,
    horizontalPadding: androidx.compose.ui.unit.Dp = 8.dp,
    verticalPadding: androidx.compose.ui.unit.Dp = 2.dp
) {
    val (statusBg, statusTextColor) = getColorsForStatus(status)
    Box(
        modifier = modifier
            .background(statusBg, RoundedCornerShape(percent = 50))
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Text(
            text = status.uppercase(),
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = statusTextColor
            )
        )
    }
}
