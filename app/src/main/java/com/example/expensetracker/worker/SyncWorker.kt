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
                            location = expense.location
                        )
                    )
                }
            }

            val supabase = SupabaseClient.supabase
            
            if (mappedProjects.isNotEmpty()) {
                supabase.from("projects").upsert(mappedProjects)
            }
            if (mappedExpenses.isNotEmpty()) {
                supabase.from("expenses").upsert(mappedExpenses)
            }

            // Sync successful
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // On network failure or other exceptions, retry later
            Result.retry()
        }
    }
}
