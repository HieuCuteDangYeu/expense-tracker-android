package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE isDeleted = 0") fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query(
            "SELECT * FROM projects WHERE isDeleted = 0 AND (projectName LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')"
    )
    fun searchProjects(searchQuery: String): Flow<List<ProjectEntity>>

    @androidx.room.Transaction
    @Query("SELECT * FROM projects WHERE projectId = :projectId AND isDeleted = 0")
    fun getProjectWithExpenses(projectId: Int): Flow<ProjectWithExpenses>

    @androidx.room.Transaction
    @Query("SELECT * FROM projects WHERE isDeleted = 0")
    suspend fun getAllProjectsWithExpenses(): List<ProjectWithExpenses>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @androidx.room.Update suspend fun updateProject(project: ProjectEntity)

    @Query("UPDATE projects SET isDeleted = 1 WHERE projectId = :id")
    suspend fun deleteProject(id: Int)

    @Query("SELECT * FROM projects WHERE isDeleted = 1")
    suspend fun getDeletedProjects(): List<ProjectEntity>

    @Query("DELETE FROM projects WHERE projectId = :id")
    suspend fun hardDeleteProject(id: Int)

    @Query("DELETE FROM projects") suspend fun deleteAllProjects()

    // Reset database: deletes from both tables.
    // Since Expenses has CASCADE on foreign key, deleting all projects deletes attached expenses.
    // But to be explicit and safe:
    @androidx.room.Transaction
    suspend fun resetDatabase() {
        deleteAllProjects()
        // We'll also call expense deletion if needed, but CASCADE handles it.
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<ProjectEntity>)

    @androidx.room.Transaction
    @Query(
        """SELECT * FROM projects 
        WHERE isDeleted = 0
        AND (:query IS NULL OR projectName LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND (:status IS NULL OR status = :status)
        AND (:manager IS NULL OR manager = :manager)
        AND (:startDate IS NULL OR startDate >= :startDate)
        AND (:endDate IS NULL OR endDate <= :endDate)"""
    )
    fun filterProjects(
        query: String?,
        status: String?,
        manager: String?,
        startDate: String?,
        endDate: String?
    ): Flow<List<ProjectWithExpenses>>

    @Query("SELECT DISTINCT manager FROM projects ORDER BY manager ASC")
    fun getAllManagers(): Flow<List<String>>
}
