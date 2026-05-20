package com.example.ainote.ui.screens.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ainote.data.model.QuizQuestion
import com.example.ainote.ui.components.AIProcessingIndicator
import com.example.ainote.ui.components.GradientButton
import com.example.ainote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    note: Note?,
    questions: List<QuizQuestion>,
    isLoading: Boolean,
    onGenerateQuiz: () -> Unit,
    onBack: () -> Unit
) {
    var currentQ by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var showResult by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var quizFinished by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Mode", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            if (questions.isEmpty() && !isLoading) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Quiz, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text("Generate Quiz", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(if (note != null) "Create MCQs from: ${note.title}" else "Select a note first",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(Modifier.height(16.dp))
                        GradientButton("Generate Quiz", onClick = onGenerateQuiz,
                            modifier = Modifier.fillMaxWidth(), icon = Icons.Default.AutoAwesome,
                            enabled = note != null)
                    }
                }
            }

            if (isLoading) { AIProcessingIndicator("Generating quiz questions...") }

            if (questions.isNotEmpty() && !quizFinished) {
                val q = questions[currentQ]
                // Progress
                LinearProgressIndicator(
                    progress = { (currentQ + 1).toFloat() / questions.size },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text("Question ${currentQ + 1} of ${questions.size}",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(Modifier.height(16.dp))

                Text(q.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                q.options.forEachIndexed { index, option ->
                    val isSelected = selectedAnswer == index
                    val isCorrect = index == q.correctAnswerIndex
                    val bgColor = when {
                        showResult && isCorrect -> NoteGreen.copy(alpha = 0.15f)
                        showResult && isSelected && !isCorrect -> ErrorLight.copy(alpha = 0.15f)
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                    val borderColor = when {
                        showResult && isCorrect -> NoteGreen
                        showResult && isSelected && !isCorrect -> ErrorLight
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> Color.Transparent
                    }

                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable(enabled = !showResult) { selectedAnswer = index },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${('A' + index)}.", fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Text(option, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            if (showResult && isCorrect) Icon(Icons.Default.CheckCircle, null, tint = NoteGreen)
                            if (showResult && isSelected && !isCorrect) Icon(Icons.Default.Cancel, null, tint = ErrorLight)
                        }
                    }
                }

                if (showResult && q.explanation.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NoteBlue.copy(alpha = 0.1f))) {
                        Row(Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Lightbulb, null, tint = NoteOrange, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(q.explanation, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                if (!showResult) {
                    GradientButton("Check Answer", onClick = {
                        if (selectedAnswer >= 0) {
                            showResult = true
                            if (selectedAnswer == q.correctAnswerIndex) score++
                        }
                    }, modifier = Modifier.fillMaxWidth(), enabled = selectedAnswer >= 0)
                } else {
                    GradientButton(
                        text = if (currentQ < questions.size - 1) "Next Question" else "See Results",
                        onClick = {
                            if (currentQ < questions.size - 1) {
                                currentQ++; selectedAnswer = -1; showResult = false
                            } else { quizFinished = true }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Results
            if (quizFinished) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EmojiEvents, null, Modifier.size(64.dp), tint = NoteYellow)
                        Spacer(Modifier.height(12.dp))
                        Text("Quiz Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("$score / ${questions.size}", style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        GradientButton("Try Again", onClick = {
                            currentQ = 0; selectedAnswer = -1; showResult = false; score = 0; quizFinished = false
                        }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
