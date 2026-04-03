package com.example.expensetracker

import android.app.Application
import com.example.expensetracker.data.AppDatabase

class ExpenseTrackerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}