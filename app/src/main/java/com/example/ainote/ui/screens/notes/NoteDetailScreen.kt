package com.example.ainote.ui.screens.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ainote.data.model.Note
import com.example.ainote.ui.components.CustomRatingBar
import com.example.ainote.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    note: Note,
    onEdit: () -> Unit,
    onAISummary: () -> Unit,
    onQuiz: () -> Unit,
    onFlashcards: () -> Unit,
    onBack: () -> Unit
) {
    val noteColor = Color(com.example.ainote.utils.Constants.NOTE_COLORS[note.colorIndex % com.example.ainote.utils.Constants.NOTE_COLORS.size])

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Note Details", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary) }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // Note header
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = noteColor.copy(alpha = 0.15f)) {
                        Text(note.subject, style = MaterialTheme.typography.labelSmall,
                            color = noteColor, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(note.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault()).format(Date(note.updatedAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    if (note.rating > 0) {
                        Spacer(Modifier.height(8.dp))
                        CustomRatingBar(rating = note.rating, onRatingChanged = {})
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            // Content
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Text(note.content, style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }

            Spacer(Modifier.height(16.dp))
            Text("AI Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                AIToolButton(Modifier.weight(1f), "Summary", Icons.Outlined.AutoAwesome, GradientStart, onAISummary)
                AIToolButton(Modifier.weight(1f), "Quiz", Icons.Outlined.Quiz, SecondaryLight, onQuiz)
                AIToolButton(Modifier.weight(1f), "Cards", Icons.Outlined.Style, TertiaryLight, onFlashcards)
            }

            if (note.summary.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Previous Summary", style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(note.summary, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AIToolButton(modifier: Modifier, label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick, modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        }
    }
}
