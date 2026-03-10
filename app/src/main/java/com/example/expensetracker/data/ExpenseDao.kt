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
    @Query("SELECT * FROM expenses WHERE parentProjectId = :projectId AND isDeleted = 0")
    fun getExpensesForProject(projectId: Int): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Update suspend fun updateExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE expenseId = :id AND isDeleted = 0")
    fun getExpenseById(id: Int): Flow<ExpenseEntity?>

    @Query("UPDATE expenses SET isDeleted = 1 WHERE expenseId = :id")
    suspend fun deleteExpense(id: Int)

    @Query("SELECT * FROM expenses WHERE isDeleted = 1")
    suspend fun getDeletedExpenses(): List<ExpenseEntity>

    @Query("DELETE FROM expenses WHERE expenseId = :id")
    suspend fun hardDeleteExpense(id: Int)

    @Query("DELETE FROM expenses") suspend fun deleteAllExpenses()

    // ── Insights Aggregation Queries ──────────────────────────────────

    /** Total sum of all expense amounts. */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE isDeleted = 0") fun getTotalExpenses(): Flow<Double>

    /** Sum of expense amounts grouped by type (Travel, Equipment, etc.). */
    @Query(
            "SELECT type AS expenseType, SUM(amount) AS totalAmount FROM expenses WHERE isDeleted = 0 GROUP BY type ORDER BY totalAmount DESC"
    )
    fun getExpensesByType(): Flow<List<ExpenseByType>>

    /** Total sum of all project budgets. */
    @Query("SELECT COALESCE(SUM(budget), 0.0) FROM projects WHERE isDeleted = 0") fun getTotalBudget(): Flow<Double>

    /** The expense type with the highest total spend. */
    @Query(
            "SELECT type AS expenseType, SUM(amount) AS totalAmount FROM expenses WHERE isDeleted = 0 GROUP BY type ORDER BY totalAmount DESC LIMIT 1"
    )
    fun getTopExpenseType(): Flow<ExpenseByType?>
}
