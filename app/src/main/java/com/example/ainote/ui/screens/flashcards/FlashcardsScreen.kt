package com.example.ainote.ui.screens.flashcards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ainote.data.model.Flashcard
import com.example.ainote.data.model.Note
import com.example.ainote.ui.components.AIProcessingIndicator
import com.example.ainote.ui.components.GradientButton
import com.example.ainote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    note: Note?,
    flashcards: List<Flashcard>,
    isLoading: Boolean,
    onGenerateFlashcards: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flashcards", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (flashcards.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Style, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(12.dp))
                            Text("Generate Flashcards", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(if (note != null) "Create cards from: ${note.title}" else "Select a note first",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(Modifier.height(16.dp))
                            GradientButton("Generate Flashcards", onClick = onGenerateFlashcards,
                                modifier = Modifier.fillMaxWidth(), icon = Icons.Default.AutoAwesome,
                                enabled = note != null)
                        }
                    }
                }
            }

            if (isLoading) { AIProcessingIndicator("Creating flashcards...") }

            if (flashcards.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { flashcards.size })

                Text("Swipe to navigate • Tap to flip",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(Modifier.height(4.dp))
                Text("Card ${pagerState.currentPage + 1} of ${flashcards.size}",
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                HorizontalPager(state = pagerState, modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp), pageSpacing = 12.dp
                ) { page ->
                    FlipCard(flashcard = flashcards[page])
                }

                Spacer(Modifier.height(12.dp))
                // Page indicators
                Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                    repeat(flashcards.size) { i ->
                        Box(
                            Modifier.padding(horizontal = 3.dp).size(if (i == pagerState.currentPage) 10.dp else 6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (i == pagerState.currentPage) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlipCard(flashcard: Flashcard) {
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400), label = "flip"
    )

    val frontColors = listOf(GradientStart, PrimaryDark)
    val backColors = listOf(SecondaryLight, SecondaryDark)

    Card(
        Modifier.fillMaxWidth().height(320.dp)
            .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
            .clickable { isFlipped = !isFlipped },
        shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier.fillMaxSize()
                .background(Brush.linearGradient(if (rotation <= 90f) frontColors else backColors))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Help, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(flashcard.front, style = MaterialTheme.typography.titleLarge,
                        color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Text("Tap to reveal", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                ) {
                    Icon(Icons.Default.Lightbulb, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(flashcard.back, style = MaterialTheme.typography.bodyLarge,
                        color = Color.White, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
