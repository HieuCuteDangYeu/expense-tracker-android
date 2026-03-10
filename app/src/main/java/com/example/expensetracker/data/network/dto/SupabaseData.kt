package com.example.expensetracker.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseProject(
    @SerialName("id") val id: String,
    @SerialName("project_name") val projectName: String,
    @SerialName("description") val description: String?,
    @SerialName("start_date") val startDate: String?,
    @SerialName("end_date") val endDate: String?,
    @SerialName("manager") val manager: String,
    @SerialName("status") val status: String,
    @SerialName("budget") val budget: Double,
    @SerialName("special_requirements") val specialRequirements: String?,
    @SerialName("client_info") val clientInfo: String?
)

@Serializable
data class SupabaseExpense(
    @SerialName("id") val id: String,
    @SerialName("project_id") val projectId: String,
    @SerialName("expense_date") val date: String,
    @SerialName("amount") val amount: Double,
    @SerialName("currency") val currency: String,
    @SerialName("expense_type") val category: String,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("claimant") val claimant: String,
    @SerialName("payment_status") val status: String,
    @SerialName("description") val description: String?,
    @SerialName("location") val location: String?,
    @SerialName("receiptUrl") val receiptUrl: String? = null
)
