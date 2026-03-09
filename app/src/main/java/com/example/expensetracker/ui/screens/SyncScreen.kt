package com.example.expensetracker.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.network.NetworkStatus
import com.example.expensetracker.ui.theme.AppTheme
import com.example.expensetracker.viewmodel.SyncStatus
import com.example.expensetracker.viewmodel.SyncViewModel

@Composable
fun SyncScreen(
    viewModel: SyncViewModel,
    modifier: Modifier = Modifier
) {
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastSynced by viewModel.lastSynced.collectAsState()
    val autoSyncWifi by viewModel.autoSyncWifi.collectAsState()
    val syncHistory by viewModel.syncHistory.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val networkStatus by viewModel.networkStatus.collectAsState()

    // Determine Status Banner parameters
    class BannerProps(
        val title: String,
        val subtitle: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val bgColor: Color,
        val borderColor: Color,
        val iconColor: Color,
        val titleColor: Color,
        val subtitleColor: Color,
        val dotColor: Color
    )

    val currentBannerProps = when (networkStatus) {
        NetworkStatus.Available_WiFi -> BannerProps(
            title = "Status: Connected to Wi-Fi",
            subtitle = "Your connection is stable",
            icon = Icons.Default.Check,
            bgColor = AppTheme.extended.successBg,
            borderColor = AppTheme.extended.successBg,
            iconColor = AppTheme.extended.successText,
            titleColor = AppTheme.extended.successText,
            subtitleColor = AppTheme.extended.successText,
            dotColor = Color(0xFF22C55E)
        )
        NetworkStatus.Available_Cellular -> BannerProps(
            title = "Status: Connected to Cellular",
            subtitle = "Auto-sync paused to save data",
            icon = Icons.Default.Warning,
            bgColor = AppTheme.extended.warningBg,
            borderColor = AppTheme.extended.warningBg,
            iconColor = AppTheme.extended.warningText,
            titleColor = AppTheme.extended.warningText,
            subtitleColor = AppTheme.extended.warningText,
            dotColor = Color(0xFFF59E0B)
        )
        NetworkStatus.Lost -> BannerProps(
            title = "Status: Offline",
            subtitle = "Network connection lost",
            icon = Icons.Default.Warning,
            bgColor = AppTheme.extended.errorBg,
            borderColor = AppTheme.extended.errorBg,
            iconColor = AppTheme.extended.errorText,
            titleColor = AppTheme.extended.errorText,
            subtitleColor = AppTheme.extended.errorText,
            dotColor = Color(0xFFEF4444)
        )
    }

        // ── Network Status Banner ──────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = currentBannerProps.bgColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, currentBannerProps.borderColor)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = currentBannerProps.icon,
                    contentDescription = null,
                    tint = currentBannerProps.iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentBannerProps.title,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = currentBannerProps.titleColor
                        )
                    )
                    Text(
                        text = currentBannerProps.subtitle,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = currentBannerProps.subtitleColor
                        )
                    )
                }
                // Status dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(currentBannerProps.dotColor)
                )
            }
        }

        // ── Sync Action Card ───────────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sync icon with circular progress
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    when (syncStatus) {
                        SyncStatus.SYNCING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                strokeWidth = 3.dp
                            )
                            val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
                            val rotation by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "rotation"
                            )
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Syncing",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp).rotate(rotation)
                            )
                        }
                        SyncStatus.SUCCESS -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = AppTheme.extended.successText,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        SyncStatus.ERROR -> {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        SyncStatus.IDLE -> {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Last synced text
                Text(
                    text = when (syncStatus) {
                        SyncStatus.SYNCING -> "Synchronizing..."
                        SyncStatus.SUCCESS -> "Sync complete!"
                        SyncStatus.ERROR -> "Sync failed. Try again."
                        SyncStatus.IDLE -> if (lastSynced != null) "Last synced $lastSynced" else "Last synced 2 minutes ago"
                    },
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = when (syncStatus) {
                            SyncStatus.SUCCESS -> AppTheme.extended.successText
                            SyncStatus.ERROR -> MaterialTheme.colorScheme.error
                            else -> AppTheme.extended.textSecondary
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sync button
                Button(
                    onClick = { viewModel.syncNow() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = AppTheme.extended.textSecondary
                    ),
                    enabled = syncStatus != SyncStatus.SYNCING && networkStatus != NetworkStatus.Lost
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Synchronize with Cloud",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // ── Settings Toggle Section ────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = AppTheme.extended.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-sync when on Wi-Fi",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Save cellular data usage",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = AppTheme.extended.textSecondary
                        )
                    )
                }
                Switch(
                    checked = autoSyncWifi,
                    onCheckedChange = { viewModel.toggleAutoSyncWifi() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        // ── Sync History ───────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "SYNC HISTORY",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.extended.textSecondary,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(start = 4.dp)
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    syncHistory.forEachIndexed { index, entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = AppTheme.extended.successText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.timestamp,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = entry.description,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = AppTheme.extended.textSecondary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = entry.status,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppTheme.extended.textSecondary
                                )
                            )
                        }
                        if (index < syncHistory.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
