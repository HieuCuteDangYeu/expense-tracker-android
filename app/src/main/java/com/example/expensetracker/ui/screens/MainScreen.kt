package com.example.expensetracker.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.ui.theme.AppTheme
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.ProjectFormViewModel
import com.example.expensetracker.viewmodel.ProjectViewModel
import com.example.expensetracker.viewmodel.SyncViewModel
import com.example.expensetracker.viewmodel.InsightsViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Projects : Screen("projects", "Projects", Icons.AutoMirrored.Filled.List)
    object Sync : Screen("sync", "Sync", Icons.Default.Refresh)
    object Insights : Screen("insights", "Insights", Icons.Default.Info)
}

val items = listOf(Screen.Projects, Screen.Sync, Screen.Insights)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
        projectViewModel: ProjectViewModel,
        projectFormViewModel: ProjectFormViewModel,
        expenseViewModel: ExpenseViewModel,
        syncViewModel: SyncViewModel,
        insightsViewModel: InsightsViewModel,
        navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
            topBar = {
                val titleText =
                        when (currentRoute) {
                            "add_project" -> "New Project"
                            "review_project" -> "Review Project"
                            Screen.Projects.route -> "Project Tracker"
                            Screen.Insights.route -> "Spending Insights"
                            Screen.Sync.route -> "Sync Center"
                            else -> "Project Tracker"
                        }
                TopAppBar(
                        title = {
                            Text(
                                    text = titleText,
                                    style =
                                            androidx.compose.ui.text.TextStyle(
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight(600),
                                                    color = MaterialTheme.colorScheme.onSurface
                                            )
                            )
                        },
                        navigationIcon = {
                            if (currentRoute != Screen.Projects.route &&
                                            currentRoute != Screen.Insights.route &&
                                            currentRoute != Screen.Sync.route
                            ) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                )
                )
            },
            bottomBar = {
                Box(contentAlignment = Alignment.TopCenter) {
                    androidx.compose.material3.Surface(
                            color = MaterialTheme.colorScheme.surface,
                            border =
                                    androidx.compose.foundation.BorderStroke(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant
                                    ),
                            contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                                                .padding(top = 8.dp, bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            items.forEach { screen ->
                                val isSelected = currentRoute == screen.route
                                val contentColor =
                                        if (isSelected)
                                                MaterialTheme.colorScheme.onSurface
                                        else AppTheme.extended.textTertiary

                                Column(
                                        modifier =
                                                Modifier.weight(1f)
                                                        .clickable(
                                                                interactionSource =
                                                                        androidx.compose.runtime
                                                                                .remember {
                                                                                    MutableInteractionSource()
                                                                                },
                                                                indication = null,
                                                                onClick = {
                                                                    navController.navigate(
                                                                            screen.route
                                                                    ) {
                                                                        popUpTo(
                                                                                navController.graph
                                                                                        .findStartDestination()
                                                                                        .id
                                                                        ) { saveState = false }
                                                                        launchSingleTop = true
                                                                        restoreState = false
                                                                    }
                                                                }
                                                        ),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom
                                ) {
                                    Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            tint = contentColor,
                                            modifier = Modifier.padding(bottom = 4.dp).size(24.dp)
                                    )
                                    Text(
                                            text = screen.title,
                                            style =
                                                    androidx.compose.ui.text.TextStyle(
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight(600),
                                                            color = contentColor
                                                    )
                                    )
                                }
                            }
                        }
                    }

                    if (currentRoute == Screen.Projects.route) {
                        Box(
                                modifier =
                                        Modifier.offset(y = (-24).dp)
                                                .size(48.dp)
                                                .background(
                                                        MaterialTheme.colorScheme.primary,
                                                        androidx.compose.foundation.shape
                                                                .CircleShape
                                                )
                                                .border(
                                                        4.dp,
                                                        MaterialTheme.colorScheme.background,
                                                        androidx.compose.foundation.shape
                                                                .CircleShape
                                                )
                                                .clickable(
                                                        interactionSource =
                                                                androidx.compose.runtime.remember {
                                                                    MutableInteractionSource()
                                                                },
                                                        indication = null,
                                                        onClick = {
                                                            navController.navigate("add_project")
                                                        }
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Project",
                                    tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val tabRoutes = items.map { it.route }
            NavHost(
                navController = navController,
                startDestination = Screen.Projects.route,
                enterTransition = {
                    val initialIndex = tabRoutes.indexOf(initialState.destination.route)
                    val targetIndex = tabRoutes.indexOf(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex != -1) {
                        if (targetIndex > initialIndex) {
                            slideInHorizontally(animationSpec = tween(400), initialOffsetX = { it }) + fadeIn(animationSpec = tween(400))
                        } else {
                            slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(400))
                        }
                    } else {
                        slideInHorizontally(animationSpec = tween(400), initialOffsetX = { it }) + fadeIn(animationSpec = tween(400))
                    }
                },
                exitTransition = {
                    val initialIndex = tabRoutes.indexOf(initialState.destination.route)
                    val targetIndex = tabRoutes.indexOf(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex != -1) {
                        if (targetIndex > initialIndex) {
                            slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(400))
                        } else {
                            slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { it }) + fadeOut(animationSpec = tween(400))
                        }
                    } else {
                        slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(400))
                    }
                },
                popEnterTransition = {
                    val initialIndex = tabRoutes.indexOf(initialState.destination.route)
                    val targetIndex = tabRoutes.indexOf(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex != -1) {
                        if (targetIndex > initialIndex) {
                            slideInHorizontally(animationSpec = tween(400), initialOffsetX = { it }) + fadeIn(animationSpec = tween(400))
                        } else {
                            slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(400))
                        }
                    } else {
                        slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(400))
                    }
                },
                popExitTransition = {
                    val initialIndex = tabRoutes.indexOf(initialState.destination.route)
                    val targetIndex = tabRoutes.indexOf(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex != -1) {
                        if (targetIndex > initialIndex) {
                            slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(400))
                        } else {
                            slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { it }) + fadeOut(animationSpec = tween(400))
                        }
                    } else {
                        slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { it }) + fadeOut(animationSpec = tween(400))
                    }
                }
            ) {
                composable(Screen.Projects.route) {
                    DashboardScreen(
                            viewModel = projectViewModel,
                            onProjectClick = { projectId ->
                                navController.navigate("project_details/$projectId")
                            },
                            onEditProject = { projectId ->
                                navController.navigate("add_project?projectId=$projectId")
                            }
                    )
                }
                composable(
                        route = "add_project?projectId={projectId}",
                        arguments =
                                listOf(
                                        androidx.navigation.navArgument("projectId") {
                                            type = androidx.navigation.NavType.StringType
                                            nullable = true
                                        }
                                ),
                        enterTransition = { slideInVertically(animationSpec = tween(400), initialOffsetY = { it }) + fadeIn(animationSpec = tween(400)) },
                        exitTransition = { slideOutVertically(animationSpec = tween(400), targetOffsetY = { it }) + fadeOut(animationSpec = tween(400)) },
                        popEnterTransition = { slideInVertically(animationSpec = tween(400), initialOffsetY = { it }) + fadeIn(animationSpec = tween(400)) },
                        popExitTransition = { slideOutVertically(animationSpec = tween(400), targetOffsetY = { it }) + fadeOut(animationSpec = tween(400)) }
                ) { backStackEntry ->
                    val projectIdStr = backStackEntry.arguments?.getString("projectId")

                    androidx.compose.runtime.LaunchedEffect(projectIdStr) {
                        if (projectIdStr != null) {
                            projectFormViewModel.loadProject(projectIdStr.toInt())
                        } else {
                            projectFormViewModel.resetForm()
                        }
                    }

                    AddProjectScreen(
                            viewModel = projectFormViewModel,
                            onNavigateBack = {
                                projectFormViewModel.resetForm()
                                navController.navigate(Screen.Projects.route) {
                                    popUpTo(Screen.Projects.route) { inclusive = true }
                                }
                            }
                    )
                }
                composable("project_details/{projectId}") { backStackEntry ->
                    val projectId =
                            backStackEntry.arguments?.getString("projectId")?.toIntOrNull()
                                    ?: return@composable
                    ProjectDetailsScreen(
                            projectId = projectId,
                            viewModel = expenseViewModel
                    )
                }
                composable(Screen.Insights.route) {
                    InsightsScreen(viewModel = insightsViewModel)
                }
                composable(Screen.Sync.route) {
                    SyncScreen(viewModel = syncViewModel)
                }
            }
        }
    }
}
