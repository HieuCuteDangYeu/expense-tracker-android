package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["projectId"],
            childColumns = ["parentProjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parentProjectId")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val expenseId: Int = 0,
    val parentProjectId: Int,
    val date: String,
    val amount: Double,
    val currency: String,
    val type: String, // Travel, Equipment, etc.
    val paymentMethod: String,
    val claimant: String,
    val paymentStatus: String, // Paid, Pending, Reimbursed
    val description: String?, // Optional
    val location: String?, // Optional
    val receiptUri: String? = null // Optional receipt image URI
)
