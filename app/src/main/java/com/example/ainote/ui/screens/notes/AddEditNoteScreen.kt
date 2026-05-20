package com.example.ainote.ui.screens.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ainote.data.model.Note
import com.example.ainote.ui.components.*
import com.example.ainote.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    note: Note?,
    onSave: (Note) -> Unit,
    onBack: () -> Unit
) {
    val isEditing = note != null
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var subject by remember { mutableStateOf(note?.subject ?: "General") }
    var rating by remember { mutableFloatStateOf(note?.rating ?: 0f) }
    var colorIndex by remember { mutableIntStateOf(note?.colorIndex ?: 0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Note" else "New Note", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                Note(
                                    id = note?.id ?: 0,
                                    title = title, content = content, subject = subject,
                                    rating = rating, colorIndex = colorIndex,
                                    summary = note?.summary ?: "",
                                    isVoiceNote = note?.isVoiceNote ?: false,
                                    createdAt = note?.createdAt ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }) {
                        Icon(Icons.Default.Check, "Save", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            CustomTextField(value = title, onValueChange = { title = it },
                label = "Title", leadingIcon = Icons.Default.Title, placeholder = "Enter note title")
            Spacer(Modifier.height(12.dp))

            SubjectDropdown(
                selectedSubject = subject, onSubjectSelected = { subject = it },
                subjects = Constants.SUBJECTS, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // Color picker
            Text("Color", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                Constants.NOTE_COLORS.forEachIndexed { index, color ->
                    Box(
                        Modifier.size(36.dp)
                            .clip(CircleShape)
                            .background(Color(color), CircleShape)
                            .then(
                                if (index == colorIndex) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                else Modifier
                            )
                            .clickable { colorIndex = index }
                    ) {
                        if (index == colorIndex) {
                            Icon(Icons.Default.Check, null, tint = Color.White,
                                modifier = Modifier.size(18.dp).align(Alignment.Center))
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Rating
            Text("Usefulness Rating", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            CustomRatingBar(rating = rating, onRatingChanged = { rating = it })
            Spacer(Modifier.height(16.dp))

            // Content
            CustomTextField(
                value = content, onValueChange = { content = it },
                label = "Content", placeholder = "Write your notes here...",
                singleLine = false, maxLines = 20, minLines = 8,
                leadingIcon = Icons.Default.Notes
            )
            Spacer(Modifier.height(24.dp))

            GradientButton(
                text = if (isEditing) "Update Note" else "Save Note",
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            Note(
                                id = note?.id ?: 0,
                                title = title, content = content, subject = subject,
                                rating = rating, colorIndex = colorIndex,
                                summary = note?.summary ?: "",
                                isVoiceNote = note?.isVoiceNote ?: false,
                                createdAt = note?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Save
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
