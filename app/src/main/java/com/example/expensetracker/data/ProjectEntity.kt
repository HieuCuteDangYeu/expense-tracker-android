package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val projectId: Int,
    val projectName: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val manager: String,
    val status: String, // Active, Completed, On Hold
    val budget: Double,
    val specialRequirements: String?, // Optional
    val clientInfo: String?, // Optional
    val priority: String, // Low, Medium, High
    val isDeleted: Boolean = false
) {
    val formattedId: String
        get() = "PRJ-${projectId.toString().padStart(4, '0')}"
}
