package com.example.ainote.ui.screens.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ainote.data.local.NoteDatabase
import com.example.ainote.data.model.Flashcard
import com.example.ainote.data.model.Note
import com.example.ainote.data.model.QuizQuestion
import com.example.ainote.utils.Constants
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AIChatMessage(val role: String, val text: String) // "user" or "model"

class AIViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao = NoteDatabase.getDatabase(application).noteDao()

    private val generativeModel = GenerativeModel(
        modelName = Constants.GEMINI_MODEL,
        apiKey = Constants.GEMINI_API_KEY
    )

    // Persistent chat session so follow-up questions retain context
    private var chatSession = generativeModel.startChat()

    private val _currentNote = MutableStateFlow<Note?>(null)
    val currentNote: StateFlow<Note?> = _currentNote

    private val _summary = MutableStateFlow("")
    val summary: StateFlow<String> = _summary

    private val _chatHistory = MutableStateFlow<List<AIChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<AIChatMessage>> = _chatHistory

    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions

    private val _flashcards = MutableStateFlow<List<Flashcard>>(emptyList())
    val flashcards: StateFlow<List<Flashcard>> = _flashcards

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading

    fun loadNote(noteId: Int) {
        viewModelScope.launch {
            _currentNote.value = noteDao.getNoteById(noteId)
        }
    }

    // ---- Summary with follow-up chat ----

    fun generateSummary() {
        val note = _currentNote.value ?: return
        _isLoading.value = true
        _chatHistory.value = emptyList()

        viewModelScope.launch {
            try {
                // Fresh chat session with note context
                chatSession = generativeModel.startChat()

                val prompt = "Summarize these study notes for a student. " +
                    "Give key bullet points, a short overview, and one study tip. " +
                    "Title: ${note.title}. Subject: ${note.subject}. Content: ${note.content}"

                val response = chatSession.sendMessage(prompt)
                val rawText = response.text ?: "Could not generate summary. Please try again."
                val text = cleanResponse(rawText) ?: rawText
                _summary.value = text

                // Store in chat history so follow-up UI appears
                _chatHistory.value = listOf(AIChatMessage("model", text))

                // Save to DB
                noteDao.updateNote(
                    note.copy(summary = text, updatedAt = System.currentTimeMillis())
                )
            } catch (e: Exception) {
                val err = "Could not generate summary. ${e.localizedMessage}"
                _summary.value = err
                _chatHistory.value = listOf(AIChatMessage("model", err))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendFollowUpQuestion(question: String) {
        if (question.isBlank() || _isChatLoading.value) return
        _isChatLoading.value = true
        _chatHistory.value = _chatHistory.value + AIChatMessage("user", question)

        viewModelScope.launch {
            try {
                val response = chatSession.sendMessage(
                    content("user") { text(question) }
                )
                val answer = cleanResponse(response.text) ?: "Could not get a response."
                _chatHistory.value = _chatHistory.value + AIChatMessage("model", answer)
            } catch (e: Exception) {
                _chatHistory.value = _chatHistory.value +
                    AIChatMessage("model", "Error: ${e.localizedMessage}")
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    // ---- Quiz ----

    fun generateQuiz() {
        val note = _currentNote.value ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val prompt = "Create 4 multiple choice questions from these notes. " +
                    "For each question write it exactly like this:\n" +
                    "QUESTION: the question\n" +
                    "A: option\nB: option\nC: option\nD: option\n" +
                    "CORRECT: A\nEXPLANATION: why\n---\n" +
                    "Notes title: ${note.title}. Content: ${note.content}"

                val response = generativeModel.generateContent(prompt)
                val text = cleanResponse(response.text) ?: ""
                _quizQuestions.value = parseQuiz(text).ifEmpty { fallbackQuiz(note) }
            } catch (e: Exception) {
                _quizQuestions.value = fallbackQuiz(note)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---- Flashcards ----

    fun generateFlashcards() {
        val note = _currentNote.value ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val prompt = "Create 6 flashcards from these notes. " +
                    "For each flashcard write exactly:\n" +
                    "FRONT: term or question\nBACK: answer or definition\n---\n" +
                    "Notes title: ${note.title}. Content: ${note.content}"

                val response = generativeModel.generateContent(prompt)
                val text = cleanResponse(response.text) ?: ""
                _flashcards.value = parseFlashcards(text, note.id).ifEmpty { fallbackFlashcards(note) }
            } catch (e: Exception) {
                _flashcards.value = fallbackFlashcards(note)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---- Response cleaning ----

    private fun cleanResponse(text: String?): String? {
        if (text == null) return null
        // Remove lines that look like the prompt was echoed back
        val lines = text.lines()
        val cleaned = lines.filter { line ->
            !line.contains("Title:", ignoreCase = true) ||
            !line.contains("Content:", ignoreCase = true) ||
            !line.contains("Notes title:", ignoreCase = true)
        }
        return cleaned.joinToString("\n").trim().ifBlank { text.trim() }
    }

    // ---- Parsers ----

    private fun parseQuiz(text: String): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()
        val blocks = text.split("---").filter { it.isNotBlank() }
        for (block in blocks) {
            try {
                val lines = block.trim().lines().filter { it.isNotBlank() }
                var question = ""
                val options = mutableListOf<String>()
                var correctIndex = 0
                var explanation = ""
                for (line in lines) {
                    val t = line.trim()
                    when {
                        t.startsWith("QUESTION:", ignoreCase = true) -> question = t.substringAfter(":").trim()
                        t.startsWith("A:", ignoreCase = true) -> options.add(t.substringAfter(":").trim())
                        t.startsWith("B:", ignoreCase = true) -> options.add(t.substringAfter(":").trim())
                        t.startsWith("C:", ignoreCase = true) -> options.add(t.substringAfter(":").trim())
                        t.startsWith("D:", ignoreCase = true) -> options.add(t.substringAfter(":").trim())
                        t.startsWith("CORRECT:", ignoreCase = true) -> {
                            val a = t.substringAfter(":").trim().uppercase()
                            correctIndex = when (a.firstOrNull()) { 'A' -> 0; 'B' -> 1; 'C' -> 2; else -> 3 }
                        }
                        t.startsWith("EXPLANATION:", ignoreCase = true) -> explanation = t.substringAfter(":").trim()
                    }
                }
                if (question.isNotBlank() && options.size >= 4)
                    questions.add(QuizQuestion(question, options.take(4), correctIndex, explanation))
            } catch (_: Exception) {}
        }
        return questions
    }

    private fun parseFlashcards(text: String, noteId: Int): List<Flashcard> {
        val cards = mutableListOf<Flashcard>()
        val blocks = text.split("---").filter { it.isNotBlank() }
        for (block in blocks) {
            try {
                var front = ""; var back = ""
                block.trim().lines().forEach { line ->
                    val t = line.trim()
                    when {
                        t.startsWith("FRONT:", ignoreCase = true) -> front = t.substringAfter(":").trim()
                        t.startsWith("BACK:", ignoreCase = true) -> back = t.substringAfter(":").trim()
                    }
                }
                if (front.isNotBlank() && back.isNotBlank())
                    cards.add(Flashcard(front, back, noteId))
            } catch (_: Exception) {}
        }
        return cards
    }

    private fun fallbackQuiz(note: Note) = listOf(
        QuizQuestion("What subject is '${note.title}' about?",
            listOf(note.subject, "History", "Science", "Mathematics"), 0, "Belongs to ${note.subject}.")
    )

    private fun fallbackFlashcards(note: Note) = listOf(
        Flashcard("What is ${note.title} about?", note.content.take(150), note.id)
    )

    fun clearData() {
        _currentNote.value = null
        _summary.value = ""
        _chatHistory.value = emptyList()
        _quizQuestions.value = emptyList()
        _flashcards.value = emptyList()
        chatSession = generativeModel.startChat()
    }
}
