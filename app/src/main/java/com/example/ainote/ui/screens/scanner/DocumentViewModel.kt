package com.example.ainote.ui.screens.scanner

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ainote.data.local.NoteDatabase
import com.example.ainote.data.model.Note
import com.example.ainote.utils.Constants
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChatMessage(val role: String, val text: String)  // role: "user" or "model"

class DocumentViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao = NoteDatabase.getDatabase(application).noteDao()
    private val context = application.applicationContext

    private val generativeModel = GenerativeModel(
        modelName = Constants.GEMINI_MODEL,
        apiKey = Constants.GEMINI_API_KEY
    )

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Chat session — started after summary is generated so context is preserved
    private var chatSession = generativeModel.startChat()

    // --- StateFlows ---
    private val _extractedText = MutableStateFlow("")
    val extractedText: StateFlow<String> = _extractedText

    private val _summary = MutableStateFlow("")
    val summary: StateFlow<String> = _summary

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _isSummarizing = MutableStateFlow(false)
    val isSummarizing: StateFlow<Boolean> = _isSummarizing

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage

    private val _pageCount = MutableStateFlow(0)
    val pageCount: StateFlow<Int> = _pageCount

    private val _processedPages = MutableStateFlow(0)
    val processedPages: StateFlow<Int> = _processedPages

    // ---- Document Processing ----

    fun processDocument(uri: Uri) {
        _isProcessing.value = true
        _extractedText.value = ""
        _summary.value = ""
        _chatHistory.value = emptyList()
        _statusMessage.value = "Opening document..."

        viewModelScope.launch {
            try {
                val mimeType = context.contentResolver.getType(uri) ?: ""
                when {
                    mimeType == "application/pdf" -> processPdf(uri)
                    mimeType.startsWith("image/") -> processImage(uri)
                    else -> processImage(uri) // Fallback: try as image
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
                _isProcessing.value = false
            }
        }
    }

    private suspend fun processPdf(uri: Uri) {
        try {
            val fileDescriptor: android.os.ParcelFileDescriptor = context.contentResolver
                .openFileDescriptor(uri, "r") ?: throw Exception("Cannot open PDF file")

            val renderer = PdfRenderer(fileDescriptor)
            _pageCount.value = renderer.pageCount
            _statusMessage.value = "Scanning ${renderer.pageCount} pages..."

            val allText = StringBuilder()
            for (i in 0 until renderer.pageCount) {
                _processedPages.value = i + 1
                _statusMessage.value = "Scanning page ${i + 1} of ${renderer.pageCount}..."

                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(
                    page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888
                )
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                try {
                    val image = InputImage.fromBitmap(bitmap, 0)
                    val result = textRecognizer.process(image).await()
                    if (result.text.isNotBlank()) {
                        allText.append("--- Page ${i + 1} ---\n${result.text}\n\n")
                    }
                } catch (_: Exception) {
                    allText.append("--- Page ${i + 1}: Could not read ---\n\n")
                }
                bitmap.recycle()
            }

            renderer.close()
            fileDescriptor.close()

            val text = allText.toString().trim()
            if (text.isBlank()) {
                _statusMessage.value = "No text found. The PDF may contain only images."
            } else {
                _extractedText.value = text
                _statusMessage.value = "✅ Extracted text from ${renderer.pageCount} pages. Tap 'Generate AI Summary'."
            }
        } catch (e: Exception) {
            _statusMessage.value = "Error reading PDF: ${e.message}"
        } finally {
            _isProcessing.value = false
        }
    }

    private suspend fun processImage(uri: Uri) {
        try {
            _statusMessage.value = "Scanning image with OCR..."
            val image = InputImage.fromFilePath(context, uri)
            val result = textRecognizer.process(image).await()

            if (result.text.isBlank()) {
                _statusMessage.value = "No text found in the image."
            } else {
                _extractedText.value = result.text
                _statusMessage.value = "✅ Text extracted. Tap 'Generate AI Summary'."
            }
        } catch (e: Exception) {
            _statusMessage.value = "Error scanning image: ${e.message}"
        } finally {
            _isProcessing.value = false
        }
    }

    // ---- AI Summary ----

    fun generateSummaryFromText() {
        val text = _extractedText.value.ifBlank { return }
        _isSummarizing.value = true
        _statusMessage.value = "AI is summarizing..."

        viewModelScope.launch {
            try {
                // Start a fresh chat session with the document context
                chatSession = generativeModel.startChat()

                val prompt = """
                    You are a helpful study assistant. I will share a document with you.
                    Please provide a clear summary using this format:
                    
                    📋 **Key Points:**
                    - (bullet point 1)
                    - (bullet point 2)
                    
                    📝 **Summary:**
                    (2-4 sentence overview)
                    
                    💡 **Study Tips:**
                    - (1-2 actionable tips)
                    
                    After this summary, I may ask you follow-up questions about this document.
                    
                    Here is the document content:
                    ${text.take(8000)}
                """.trimIndent()

                val response = chatSession.sendMessage(prompt)
                val summaryText = response.text ?: "Unable to generate summary."
                _summary.value = summaryText
                _statusMessage.value = "✅ Summary ready! Ask any follow-up question below."

                // Seed the visible chat history with the summary
                _chatHistory.value = listOf(
                    ChatMessage("model", summaryText)
                )
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.localizedMessage}"
                _summary.value = ""
            } finally {
                _isSummarizing.value = false
            }
        }
    }

    // ---- Follow-up Chat ----

    fun sendFollowUpQuestion(question: String) {
        if (question.isBlank() || _isChatLoading.value) return
        _isChatLoading.value = true

        // Add user message immediately
        _chatHistory.value = _chatHistory.value + ChatMessage("user", question)

        viewModelScope.launch {
            try {
                val response = chatSession.sendMessage(
                    content("user") { text(question) }
                )
                val answer = response.text ?: "I couldn't understand that. Try rephrasing."
                _chatHistory.value = _chatHistory.value + ChatMessage("model", answer)
            } catch (e: Exception) {
                _chatHistory.value = _chatHistory.value + ChatMessage(
                    "model", "Error: ${e.localizedMessage ?: "Something went wrong. Try again."}"
                )
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    // ---- Save as Note ----

    fun saveAsNote(title: String, subject: String) {
        viewModelScope.launch {
            val content = buildString {
                if (_summary.value.isNotBlank()) {
                    append("--- AI Summary ---\n${_summary.value}\n\n")
                }
                append("--- Extracted Text ---\n${_extractedText.value}")
            }
            val note = Note(
                title = title.ifBlank { "Scanned Document" },
                content = content,
                subject = subject.ifBlank { "General" },
                summary = _summary.value,
                rating = 0f,
                colorIndex = 4
            )
            noteDao.insertNote(note)
            _statusMessage.value = "✅ Saved to My Notes!"
        }
    }

    fun clearAll() {
        _extractedText.value = ""
        _summary.value = ""
        _chatHistory.value = emptyList()
        _statusMessage.value = ""
        _pageCount.value = 0
        _processedPages.value = 0
        chatSession = generativeModel.startChat()
    }
}
