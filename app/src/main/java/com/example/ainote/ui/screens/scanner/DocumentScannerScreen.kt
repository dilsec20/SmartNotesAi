package com.example.ainote.ui.screens.scanner

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ainote.ui.components.GradientButton
import com.example.ainote.ui.components.CustomTextField
import com.example.ainote.ui.theme.GradientEnd
import com.example.ainote.ui.theme.GradientStart
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScannerScreen(
    viewModel: DocumentViewModel,
    onBack: () -> Unit,
    onNoteSaved: () -> Unit
) {
    val extractedText by viewModel.extractedText.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val isSummarizing by viewModel.isSummarizing.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val processedPages by viewModel.processedPages.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()

    var noteTitle by remember { mutableStateOf("") }
    var noteSubject by remember { mutableStateOf("") }
    var chatInput by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom when new chat messages arrive
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.processDocument(it) } }

    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.processDocument(it) } }

    // Save Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save as Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = "Note Title"
                    )
                    CustomTextField(
                        value = noteSubject,
                        onValueChange = { noteSubject = it },
                        label = "Subject (e.g. Physics)"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveAsNote(noteTitle, noteSubject)
                    showSaveDialog = false
                    onNoteSaved()
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Document", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearAll()
                        onBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    if (summary.isNotBlank()) {
                        IconButton(onClick = { showSaveDialog = true }) {
                            Icon(Icons.Default.Save, "Save Note")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Top section (fixed height)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pick document buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { pdfLauncher.launch("application/pdf") },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing && !isSummarizing
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("PDF")
                    }
                    OutlinedButton(
                        onClick = { imageLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing && !isSummarizing
                    ) {
                        Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Image")
                    }
                }

                // Status / progress
                if (isProcessing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { if (pageCount > 0) processedPages.toFloat() / pageCount else 0f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(statusMessage, style = MaterialTheme.typography.labelSmall)
                    }
                } else if (statusMessage.isNotBlank() && summary.isBlank()) {
                    Text(
                        statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Generate Summary button — shown after text extracted but before summary
                if (extractedText.isNotBlank() && summary.isBlank() && !isProcessing) {
                    GradientButton(
                        text = if (isSummarizing) "Generating Summary..." else "✨ Generate AI Summary",
                        onClick = { viewModel.generateSummaryFromText() },
                        enabled = !isSummarizing,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isSummarizing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            HorizontalDivider()

            // Chat area — shows summary + follow-up messages
            if (chatHistory.isNotEmpty() || isSummarizing) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(chatHistory) { msg ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                        ) {
                            ChatBubble(msg)
                        }
                    }

                    if (isChatLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Thinking...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Chat input bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Ask a follow-up question...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        singleLine = false,
                        enabled = !isChatLoading
                    )
                    IconButton(
                        onClick = {
                            val q = chatInput.trim()
                            if (q.isNotBlank()) {
                                chatInput = ""
                                viewModel.sendFollowUpQuestion(q)
                                scope.launch {
                                    listState.animateScrollToItem(chatHistory.size)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                        enabled = chatInput.isNotBlank() && !isChatLoading
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White)
                    }
                }
            } else if (summary.isBlank() && extractedText.isBlank()) {
                // Empty state
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(
                            Icons.Default.DocumentScanner,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                        Text("Upload a PDF or Image", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "The app will extract text via OCR\nand generate an AI summary",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 310.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = msg.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
