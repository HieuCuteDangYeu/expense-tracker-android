package com.example.expensetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ProjectEntity::class, ExpenseEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(ProjectDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class ProjectDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.projectDao())
                }
            }
        }

        suspend fun populateDatabase(projectDao: ProjectDao) {
            val projects = listOf(
                ProjectEntity(9042, "Skyline Apartment Remodel", "A major overhaul of the downtown skyline apartments.", "2023-01-01", "2023-12-31", "Alice Smith", "Active", 20000.0, "Needs high-end finishes", "Skyline Corp", "High"),
                ProjectEntity(2281, "Downtown Office Expansion", "Expanding the main office space.", "2023-05-01", "2024-05-01", "Bob Johnson", "On Hold", 50000.0, null, "TechNova", "Low"),
                ProjectEntity(1103, "Cloud Platform Migration", "Migrating legacy systems to AWS.", "2023-02-15", "2023-08-15", "Charlie Brown", "Active", 10000.0, "Zero downtime required", "Internal", "Medium"),
                ProjectEntity(5501, "Marketing Campaign Q1", "Q1 Social Media Marketing.", "2023-01-01", "2023-03-31", "Diana Prince", "Completed", 5000.0, null, "Marketing Dept", "Medium")
            )
            projectDao.insertAll(projects)
        }
    }
}
