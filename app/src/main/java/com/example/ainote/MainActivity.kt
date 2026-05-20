package com.example.ainote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ainote.data.local.DataStoreManager
import com.example.ainote.notifications.NotificationHelper
import com.example.ainote.ui.navigation.AppDrawerContent
import com.example.ainote.ui.navigation.NavGraph
import com.example.ainote.ui.navigation.Screen
import com.example.ainote.ui.screens.notes.NotesViewModel
import com.example.ainote.ui.screens.quiz.AIViewModel
import com.example.ainote.ui.theme.AiNoteTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        enableEdgeToEdge()
        setContent {
            val dataStoreManager = remember { DataStoreManager(applicationContext) }
            val userName by dataStoreManager.userName.collectAsState(initial = "Student")
            val isDarkMode by dataStoreManager.isDarkMode.collectAsState(initial = false)
            val scope = rememberCoroutineScope()

            val notesViewModel: NotesViewModel = viewModel()
            val aiViewModel: AIViewModel = viewModel()
            val notesCount by notesViewModel.notesCount.collectAsState()

            AiNoteTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                // Only show drawer on main screens
                val showDrawer = currentRoute in listOf(
                    Screen.Dashboard.route,
                    Screen.Notes.route,
                    Screen.Reminder.route,
                    Screen.Profile.route
                )

                if (showDrawer) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            AppDrawerContent(
                                userName = userName,
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(Screen.Dashboard.route)
                                        launchSingleTop = true
                                    }
                                },
                                onClose = { scope.launch { drawerState.close() } }
                            )
                        }
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            NavGraph(
                                navController = navController,
                                userName = userName,
                                isDarkMode = isDarkMode,
                                notesCount = notesCount,
                                onUserNameChange = { scope.launch { dataStoreManager.setUserName(it) } },
                                onDarkModeChange = { scope.launch { dataStoreManager.setDarkMode(it) } },
                                notesViewModel = notesViewModel,
                                aiViewModel = aiViewModel
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavGraph(
                            navController = navController,
                            userName = userName,
                            isDarkMode = isDarkMode,
                            notesCount = notesCount,
                            onUserNameChange = { scope.launch { dataStoreManager.setUserName(it) } },
                            onDarkModeChange = { scope.launch { dataStoreManager.setDarkMode(it) } },
                            notesViewModel = notesViewModel,
                            aiViewModel = aiViewModel
                        )
                    }
                }
            }
        }
    }
}