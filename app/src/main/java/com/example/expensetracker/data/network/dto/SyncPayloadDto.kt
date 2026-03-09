package com.example.expensetracker.data.network.dto

import com.google.gson.annotations.SerializedName

data class SyncPayloadDto(
    @SerializedName("projects")
    val projects: List<ProjectDto>
)

data class ProjectDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("projectName")
    val projectName: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("priority")
    val priority: String,
    @SerializedName("manager")
    val manager: String,
    @SerializedName("startDate")
    val startDate: String,
    @SerializedName("endDate")
    val endDate: String,
    @SerializedName("budget")
    val budget: Double,
    @SerializedName("description")
    val description: String,
    @SerializedName("expenses")
    val expenses: List<ExpenseDto>
)

data class ExpenseDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("projectId")
    val projectId: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("category")
    val category: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("paymentMethod")
    val paymentMethod: String,
    @SerializedName("claimant")
    val claimant: String,
    @SerializedName("status")
    val status: String
)
