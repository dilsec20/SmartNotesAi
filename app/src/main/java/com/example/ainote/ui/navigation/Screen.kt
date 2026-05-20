package com.example.ainote.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Notes : Screen("notes")
    data object AddEditNote : Screen("add_edit_note?noteId={noteId}") {
        fun createRoute(noteId: Int = -1) = "add_edit_note?noteId=$noteId"
    }
    data object AISummary : Screen("ai_summary/{noteId}") {
        fun createRoute(noteId: Int) = "ai_summary/$noteId"
    }
    data object Quiz : Screen("quiz/{noteId}") {
        fun createRoute(noteId: Int) = "quiz/$noteId"
    }
    data object Flashcards : Screen("flashcards/{noteId}") {
        fun createRoute(noteId: Int) = "flashcards/$noteId"
    }
    data object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Int) = "note_detail/$noteId"
    }
    data object Reminder : Screen("reminder")
    data object Profile : Screen("profile")
    data object Scanner : Screen("scanner")
}
