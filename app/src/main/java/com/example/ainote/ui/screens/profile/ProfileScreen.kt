package com.example.ainote.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ainote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    isDarkMode: Boolean,
    notesCount: Int,
    onNameChange: (String) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var editingName by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(userName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            // Profile header
            Box(
                Modifier.fillMaxWidth().height(180.dp)
                    .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
            ) {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier.size(72.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(userName.take(1).uppercase(), fontSize = 28.sp,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Student", style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f))
                }
            }

            // Stats
            Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceEvenly) {
                StatItem("Notes", notesCount.toString(), Icons.Outlined.Description)
                StatItem("Quizzes", "0", Icons.Outlined.Quiz)
                StatItem("Streak", "0", Icons.Outlined.LocalFireDepartment)
            }

            Spacer(Modifier.height(8.dp))

            // Settings
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                // Edit name
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    if (editingName) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = nameInput, onValueChange = { nameInput = it },
                                label = { Text("Name") }, modifier = Modifier.weight(1f),
                                singleLine = true, shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = {
                                onNameChange(nameInput); editingName = false
                            }) { Icon(Icons.Default.Check, "Save", tint = MaterialTheme.colorScheme.primary) }
                        }
                    } else {
                        SettingsItem(Icons.Outlined.Person, "Name", userName) { editingName = true }
                    }
                }

                Spacer(Modifier.height(8.dp))
                // Dark mode
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DarkMode, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text("Dark Mode", fontWeight = FontWeight.Medium)
                        }
                        Switch(checked = isDarkMode, onCheckedChange = onDarkModeToggle)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    SettingsItem(Icons.Outlined.Info, "Version", "1.0.0") {}
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
            .let { if (onClick != {}) it else it },
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Medium)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onClick, Modifier.size(24.dp)) {
                Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp))
            }
        }
    }
}
