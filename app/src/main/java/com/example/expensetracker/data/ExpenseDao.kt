package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE parentProjectId = :projectId")
    fun getExpensesForProject(projectId: Int): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update suspend fun updateExpense(expense: ExpenseEntity)

    @Delete suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses") suspend fun deleteAllExpenses()

    // ── Insights Aggregation Queries ──────────────────────────────────

    /** Total sum of all expense amounts. */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses") fun getTotalExpenses(): Flow<Double>

    /** Sum of expense amounts grouped by type (Travel, Equipment, etc.). */
    @Query(
            "SELECT type AS expenseType, SUM(amount) AS totalAmount FROM expenses GROUP BY type ORDER BY totalAmount DESC"
    )
    fun getExpensesByType(): Flow<List<ExpenseByType>>

    /** Total sum of all project budgets. */
    @Query("SELECT COALESCE(SUM(budget), 0.0) FROM projects") fun getTotalBudget(): Flow<Double>

    /** The expense type with the highest total spend. */
    @Query(
            "SELECT type AS expenseType, SUM(amount) AS totalAmount FROM expenses GROUP BY type ORDER BY totalAmount DESC LIMIT 1"
    )
    fun getTopExpenseType(): Flow<ExpenseByType?>
}
