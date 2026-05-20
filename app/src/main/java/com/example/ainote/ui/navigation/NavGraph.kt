package com.example.ainote.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ainote.ui.screens.auth.LoginScreen
import com.example.ainote.ui.screens.dashboard.DashboardScreen
import com.example.ainote.ui.screens.flashcards.FlashcardsScreen
import com.example.ainote.ui.screens.notes.AddEditNoteScreen
import com.example.ainote.ui.screens.notes.NoteDetailScreen
import com.example.ainote.ui.screens.notes.NotesScreen
import com.example.ainote.ui.screens.notes.NotesViewModel
import com.example.ainote.ui.screens.profile.ProfileScreen
import com.example.ainote.ui.screens.quiz.AIViewModel
import com.example.ainote.ui.screens.quiz.QuizScreen
import com.example.ainote.ui.screens.scanner.DocumentScannerScreen
import com.example.ainote.ui.screens.scanner.DocumentViewModel
import com.example.ainote.ui.screens.reminder.ReminderScreen
import com.example.ainote.ui.screens.splash.SplashScreen
import com.example.ainote.ui.screens.summary.AISummaryScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    userName: String,
    isDarkMode: Boolean,
    notesCount: Int,
    onUserNameChange: (String) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    notesViewModel: NotesViewModel = viewModel(),
    aiViewModel: AIViewModel = viewModel(),
    documentViewModel: DocumentViewModel = viewModel()
) {
    val notes by notesViewModel.filteredNotes.collectAsState()
    val searchQuery by notesViewModel.searchQuery.collectAsState()
    val selectedSubject by notesViewModel.selectedSubject.collectAsState()
    val currentNote by notesViewModel.currentNote.collectAsState()

    val aiNote by aiViewModel.currentNote.collectAsState()
    val summary by aiViewModel.summary.collectAsState()
    val chatHistory by aiViewModel.chatHistory.collectAsState()
    val quizQuestions by aiViewModel.quizQuestions.collectAsState()
    val flashcards by aiViewModel.flashcards.collectAsState()
    val isAiLoading by aiViewModel.isLoading.collectAsState()
    val isChatLoading by aiViewModel.isChatLoading.collectAsState()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(onSplashFinished = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = { name ->
                onUserNameChange(name)
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                userName = userName,
                notesCount = notesCount,
                onNotesClick = { navController.navigate(Screen.Notes.route) },
                onAddNoteClick = { navController.navigate(Screen.AddEditNote.createRoute()) },
                onQuizClick = { navController.navigate(Screen.Notes.route) },
                onFlashcardsClick = { navController.navigate(Screen.Notes.route) },
                onReminderClick = { navController.navigate(Screen.Reminder.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onScannerClick = { navController.navigate(Screen.Scanner.route) }
            )
        }

        composable(Screen.Notes.route) {
            NotesScreen(
                notes = notes,
                searchQuery = searchQuery,
                selectedSubject = selectedSubject,
                onSearchQueryChange = { notesViewModel.setSearchQuery(it) },
                onSubjectChange = { notesViewModel.setSelectedSubject(it) },
                onNoteClick = { note ->
                    navController.navigate(Screen.NoteDetail.createRoute(note.id))
                },
                onAddClick = { navController.navigate(Screen.AddEditNote.createRoute()) },
                onDeleteNote = { notesViewModel.deleteNote(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddEditNote.route,
            arguments = listOf(navArgument("noteId") {
                type = NavType.IntType; defaultValue = -1
            })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1

            LaunchedEffect(noteId) {
                if (noteId != -1) notesViewModel.loadNote(noteId)
                else notesViewModel.clearCurrentNote()
            }

            AddEditNoteScreen(
                note = if (noteId != -1) currentNote else null,
                onSave = { note ->
                    notesViewModel.saveNote(note)
                    navController.popBackStack()
                },
                onBack = {
                    notesViewModel.clearCurrentNote()
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: return@composable
            LaunchedEffect(noteId) { notesViewModel.loadNote(noteId) }

            currentNote?.let { note ->
                NoteDetailScreen(
                    note = note,
                    onEdit = {
                        navController.navigate(Screen.AddEditNote.createRoute(note.id))
                    },
                    onAISummary = {
                        navController.navigate(Screen.AISummary.createRoute(note.id))
                    },
                    onQuiz = {
                        navController.navigate(Screen.Quiz.createRoute(note.id))
                    },
                    onFlashcards = {
                        navController.navigate(Screen.Flashcards.createRoute(note.id))
                    },
                    onBack = {
                        notesViewModel.clearCurrentNote()
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            route = Screen.AISummary.route,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: return@composable
            LaunchedEffect(noteId) { aiViewModel.loadNote(noteId) }

            AISummaryScreen(
                note = aiNote,
                isLoading = isAiLoading,
                isChatLoading = isChatLoading,
                summary = summary,
                chatHistory = chatHistory,
                onGenerateSummary = { aiViewModel.generateSummary() },
                onSendFollowUp = { aiViewModel.sendFollowUpQuestion(it) },
                onBack = { aiViewModel.clearData(); navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Quiz.route,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: return@composable
            LaunchedEffect(noteId) { aiViewModel.loadNote(noteId) }

            QuizScreen(
                note = aiNote,
                questions = quizQuestions,
                isLoading = isAiLoading,
                onGenerateQuiz = { aiViewModel.generateQuiz() },
                onBack = { aiViewModel.clearData(); navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Flashcards.route,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: return@composable
            LaunchedEffect(noteId) { aiViewModel.loadNote(noteId) }

            FlashcardsScreen(
                note = aiNote,
                flashcards = flashcards,
                isLoading = isAiLoading,
                onGenerateFlashcards = { aiViewModel.generateFlashcards() },
                onBack = { aiViewModel.clearData(); navController.popBackStack() }
            )
        }

        composable(Screen.Reminder.route) {
            ReminderScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                userName = userName,
                isDarkMode = isDarkMode,
                notesCount = notesCount,
                onNameChange = onUserNameChange,
                onDarkModeToggle = onDarkModeChange,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Scanner.route) {
            DocumentScannerScreen(
                viewModel = documentViewModel,
                onBack = { 
                    documentViewModel.clearAll()
                    navController.popBackStack() 
                },
                onNoteSaved = {
                    documentViewModel.clearAll()
                    navController.navigate(Screen.Notes.route) {
                        popUpTo(Screen.Dashboard.route)
                    }
                }
            )
        }
    }
}
