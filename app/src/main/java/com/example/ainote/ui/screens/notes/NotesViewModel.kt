package com.example.ainote.ui.screens.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ainote.data.local.NoteDatabase
import com.example.ainote.data.model.Note
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao = NoteDatabase.getDatabase(application).noteDao()

    val allNotes: StateFlow<List<Note>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notesCount: StateFlow<Int> = noteDao.getNotesCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedSubject = MutableStateFlow("All")
    val selectedSubject: StateFlow<String> = _selectedSubject

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredNotes: StateFlow<List<Note>> = combine(
        allNotes, _selectedSubject, _searchQuery
    ) { notes, subject, query ->
        var result = notes
        if (subject != "All") {
            result = result.filter { it.subject == subject }
        }
        if (query.isNotBlank()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentNote = MutableStateFlow<Note?>(null)
    val currentNote: StateFlow<Note?> = _currentNote

    fun setSelectedSubject(subject: String) { _selectedSubject.value = subject }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun loadNote(noteId: Int) {
        viewModelScope.launch {
            _currentNote.value = noteDao.getNoteById(noteId)
        }
    }

    fun saveNote(note: Note, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = noteDao.insertNote(note)
            onComplete(id)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { noteDao.deleteNote(note) }
    }

    fun clearCurrentNote() { _currentNote.value = null }
}
