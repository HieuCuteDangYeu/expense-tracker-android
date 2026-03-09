package com.example.expensetracker.data

import androidx.room.Embedded
import androidx.room.Relation

data class ProjectWithExpenses(
    @Embedded val project: ProjectEntity,
    @Relation(
        parentColumn = "projectId",
        entityColumn = "parentProjectId"
    )
    val expenses: List<ExpenseEntity>
)
