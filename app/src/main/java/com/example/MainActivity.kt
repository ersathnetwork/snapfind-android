package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.SnapFindTheme
import com.example.viewmodel.LibraryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check for incoming share intent
        var initialImportUri: Uri? = null
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            initialImportUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        }

        setContent {
            val viewModel: LibraryViewModel = viewModel()
            val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val colorTheme by viewModel.colorTheme.collectAsState()

            SnapFindTheme(themeMode = themeMode, colorTheme = colorTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // If launched via share intent, navigate to import immediately after onboarding
                    LaunchedEffect(initialImportUri, onboardingCompleted) {
                        if (initialImportUri != null && onboardingCompleted) {
                            val encoded = Uri.encode(initialImportUri.toString())
                            navController.navigate("import/$encoded")
                            initialImportUri = null
                        }
                    }

                    val startDestination = if (!onboardingCompleted) "onboarding" else "library"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("onboarding") {
                            OnboardingScreen(
                                onFinish = {
                                    viewModel.completeOnboarding()
                                    navController.navigate("library") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("library") {
                            LibraryScreen(
                                viewModel = viewModel,
                                onNavigateDetail = { id -> navController.navigate("detail/$id") },
                                onNavigateImport = { encodedUri -> navController.navigate("import/$encodedUri") },
                                onNavigateFavorites = {
                                    navController.navigate("favorites") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onNavigateSettings = {
                                    navController.navigate("settings") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        composable("favorites") {
                            FavoritesScreen(
                                viewModel = viewModel,
                                onNavigateDetail = { id -> navController.navigate("detail/$id") },
                                onNavigateLibrary = {
                                    navController.navigate("library") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onNavigateSettings = {
                                    navController.navigate("settings") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateLibrary = {
                                    navController.navigate("library") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onNavigateFavorites = {
                                    navController.navigate("favorites") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        composable(
                            route = "import/{encodedUri}",
                            arguments = listOf(navArgument("encodedUri") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val encodedUri = backStackEntry.arguments?.getString("encodedUri") ?: ""
                            ImportScreen(
                                encodedUri = encodedUri,
                                onBack = { navController.popBackStack() },
                                onNavigateReview = { id, _, _ ->
                                    navController.navigate("review/$id") {
                                        popUpTo("library")
                                    }
                                }
                            )
                        }
                        composable(
                            route = "review/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id") ?: ""
                            ReviewScreen(
                                screenshotId = id,
                                onBack = { navController.popBackStack() },
                                onSaveSuccess = {
                                    navController.navigate("library") {
                                        popUpTo("library") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(
                            route = "detail/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id") ?: ""
                            DetailScreen(
                                screenshotId = id,
                                onBack = { navController.popBackStack() },
                                onEdit = { editId -> navController.navigate("review/$editId") }
                            )
                        }
                    }
                }
            }
        }
    }
}
