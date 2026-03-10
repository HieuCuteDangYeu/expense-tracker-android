package com.example.expensetracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.network.SupabaseClient
import com.example.expensetracker.data.network.dto.SupabaseExpense
import com.example.expensetracker.data.network.dto.SupabaseProject
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO))
            val projectDao = database.projectDao()
            val expenseDao = database.expenseDao()
            val supabase = SupabaseClient.supabase

            // --- 1. PULL FROM CLOUD ---
            val cloudProjects = supabase.from("projects").select().decodeList<SupabaseProject>()
            val cloudExpenses = supabase.from("expenses").select().decodeList<SupabaseExpense>()

            val entityProjects = cloudProjects.map {
                com.example.expensetracker.data.ProjectEntity(
                    projectId = it.id.toIntOrNull() ?: 0,
                    projectName = it.projectName,
                    description = it.description ?: "",
                    startDate = it.startDate ?: "",
                    endDate = it.endDate ?: "",
                    manager = it.manager,
                    status = it.status,
                    budget = it.budget,
                    specialRequirements = it.specialRequirements,
                    clientInfo = it.clientInfo,
                    priority = "Medium" // Default fallback since priority is not in DTO
                )
            }

            val entityExpenses = cloudExpenses.map {
                com.example.expensetracker.data.ExpenseEntity(
                    expenseId = it.id.toIntOrNull() ?: 0,
                    parentProjectId = it.projectId.toIntOrNull() ?: 0,
                    date = it.date,
                    amount = it.amount,
                    currency = it.currency,
                    type = it.category,
                    paymentMethod = it.paymentMethod,
                    claimant = it.claimant,
                    paymentStatus = it.status,
                    description = it.description,
                    location = it.location,
                    receiptUri = it.receiptUri
                )
            }

            if (entityProjects.isNotEmpty()) {
                projectDao.insertAll(entityProjects)
            }
            if (entityExpenses.isNotEmpty()) {
                expenseDao.insertAll(entityExpenses)
            }

            // --- 2. PUSH TO CLOUD ---
            val projectsWithExpenses = projectDao.getAllProjectsWithExpenses()

            val mappedProjects = mutableListOf<SupabaseProject>()
            val mappedExpenses = mutableListOf<SupabaseExpense>()

            projectsWithExpenses.forEach { relation ->
                val project = relation.project
                mappedProjects.add(
                    SupabaseProject(
                        id = project.projectId.toString(),
                        projectName = project.projectName,
                        description = project.description,
                        startDate = project.startDate,
                        endDate = project.endDate,
                        manager = project.manager,
                        status = project.status,
                        budget = project.budget,
                        specialRequirements = project.specialRequirements,
                        clientInfo = project.clientInfo
                    )
                )

                relation.expenses.forEach { expense ->
                    mappedExpenses.add(
                        SupabaseExpense(
                            id = expense.expenseId.toString(),
                            projectId = project.projectId.toString(),
                            date = expense.date,
                            amount = expense.amount,
                            currency = expense.currency,
                            category = expense.type,
                            paymentMethod = expense.paymentMethod,
                            claimant = expense.claimant,
                            status = expense.paymentStatus,
                            description = expense.description,
                            location = expense.location,
                            receiptUri = expense.receiptUri
                        )
                    )
                }
            }
            
            if (mappedProjects.isNotEmpty()) {
                supabase.from("projects").upsert(mappedProjects)
            }
            if (mappedExpenses.isNotEmpty()) {
                supabase.from("expenses").upsert(mappedExpenses)
            }

            // Sync successful
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("SyncError", "Supabase sync failed", e)
            val errorData = androidx.work.workDataOf("error" to (e.localizedMessage ?: "Unknown error"))
            Result.failure(errorData)
        }
    }
}
