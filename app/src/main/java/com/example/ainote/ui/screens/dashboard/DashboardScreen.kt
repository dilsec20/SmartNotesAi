package com.example.ainote.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ainote.ui.theme.*

@Composable
fun DashboardScreen(
    userName: String,
    notesCount: Int,
    onNotesClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onQuizClick: () -> Unit,
    onFlashcardsClick: () -> Unit,
    onReminderClick: () -> Unit,
    onProfileClick: () -> Unit,
    onScannerClick: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            Modifier.fillMaxWidth().height(200.dp)
                .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
        ) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Bottom) {
                Text("Hello,", fontSize = 18.sp, color = Color.White.copy(alpha = 0.8f))
                Text(userName, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text("Ready to study? You have $notesCount notes 📚",
                    style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
            }
        }

        Column(Modifier.padding(horizontal = 20.dp).offset(y = (-20).dp)) {
            // Quick actions row
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceEvenly) {
                    QuickAction(Icons.Default.Add, "New Note", NoteGreen) { onAddNoteClick() }
                    QuickAction(Icons.Default.Quiz, "Quiz", NoteBlue) { onQuizClick() }
                    QuickAction(Icons.Default.Style, "Cards", NotePink) { onFlashcardsClick() }
                    QuickAction(Icons.Default.Notifications, "Remind", NoteOrange) { onReminderClick() }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Features", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            // Feature cards grid
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                FeatureCard(Modifier.weight(1f), "My Notes", "$notesCount notes", Icons.Outlined.Description,
                    listOf(GradientStart, PrimaryDark)) { onNotesClick() }
                FeatureCard(Modifier.weight(1f), "AI Summary", "Summarize notes", Icons.Outlined.AutoAwesome,
                    listOf(SecondaryLight, SecondaryDark)) { onNotesClick() }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                FeatureCard(Modifier.weight(1f), "Quiz Mode", "Test yourself", Icons.Outlined.Quiz,
                    listOf(TertiaryLight, TertiaryDark)) { onQuizClick() }
                FeatureCard(Modifier.weight(1f), "Flashcards", "Quick revision", Icons.Outlined.Style,
                    listOf(Color(0xFFE84393), Color(0xFFFD79A8))) { onFlashcardsClick() }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                FeatureCard(Modifier.weight(1f), "Reminders", "Stay on track", Icons.Outlined.Alarm,
                    listOf(NoteOrange, NoteYellow)) { onReminderClick() }
                FeatureCard(Modifier.weight(1f), "Profile", "Settings", Icons.Outlined.Person,
                    listOf(Color(0xFF636E72), Color(0xFFB2BEC3))) { onProfileClick() }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                FeatureCard(Modifier.weight(1f), "Scan Doc", "OCR & Summary", Icons.Outlined.DocumentScanner,
                    listOf(Color(0xFF00B894), Color(0xFF55EFC4))) { onScannerClick() }
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(8.dp)
    ) {
        Box(
            Modifier.size(48.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FeatureCard(
    modifier: Modifier, title: String, subtitle: String,
    icon: ImageVector, gradientColors: List<Color>, onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(130.dp).clip(RoundedCornerShape(20.dp)).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier.fillMaxSize().background(Brush.linearGradient(gradientColors)).padding(16.dp)
        ) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Icon(icon, title, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(28.dp))
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall)
                    Text(subtitle, color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
