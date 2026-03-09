package com.example.expensetracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.expensetracker.ui.screens.MainScreen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.ProjectViewModel
import com.example.expensetracker.viewmodel.ProjectViewModelFactory
import com.example.expensetracker.viewmodel.ProjectFormViewModel
import com.example.expensetracker.viewmodel.ProjectFormViewModelFactory
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.ExpenseViewModelFactory
import com.example.expensetracker.viewmodel.SyncViewModel
import com.example.expensetracker.viewmodel.SyncViewModelFactory
import com.example.expensetracker.viewmodel.InsightsViewModel
import com.example.expensetracker.viewmodel.InsightsViewModelFactory
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private val projectViewModel: ProjectViewModel by viewModels {
        ProjectViewModelFactory((application as ExpenseTrackerApplication).database.projectDao())
    }

    private val projectFormViewModel: ProjectFormViewModel by viewModels {
        ProjectFormViewModelFactory((application as ExpenseTrackerApplication).database.projectDao())
    }

    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory(
            (application as ExpenseTrackerApplication).database.projectDao(),
            (application as ExpenseTrackerApplication).database.expenseDao()
        )
    }

    private val syncViewModel: SyncViewModel by viewModels {
        SyncViewModelFactory(
            application,
            (application as ExpenseTrackerApplication).database.projectDao()
        )
    }

    private val insightsViewModel: InsightsViewModel by viewModels {
        InsightsViewModelFactory(
            application,
            (application as ExpenseTrackerApplication).database.expenseDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                MainScreen(
                    projectViewModel = projectViewModel,
                    projectFormViewModel = projectFormViewModel,
                    expenseViewModel = expenseViewModel,
                    syncViewModel = syncViewModel,
                    insightsViewModel = insightsViewModel
                )
            }
        }
    }
}