# 📘 SmartNotes AI – Notes Summarizer & Study Assistant

## 📱 What This App Does

SmartNotes AI is an **AI-powered Android study assistant** that helps students create, organize, summarize, and revise study notes. Users can write notes, scan PDFs and images with OCR, get AI-generated summaries, take auto-generated quizzes, flip through flashcards, and set study reminders — all within one app.

---

## 🏗 Architecture: MVVM (Model-View-ViewModel)

**Why MVVM?**
- Separates UI logic from business logic, making the code testable and maintainable.
- The **Model** layer (Room Database, DataStore) handles data.
- The **View** layer (Jetpack Compose screens) displays the UI.
- The **ViewModel** layer (`NotesViewModel`, `AIViewModel`) connects them, surviving configuration changes like screen rotations.

```
User → View (Compose Screen) → ViewModel → Model (Room DB / API)
                ↑                                    |
                └────────── StateFlow ───────────────┘
```

---

## 🧩 Project Structure & Why Each File Exists

```
com.example.ainote/
├── MainActivity.kt              → Single Activity (entry point)
├── data/
│   ├── local/
│   │   ├── NoteDao.kt           → Database queries (CRUD)
│   │   ├── NoteDatabase.kt      → Room Database singleton
│   │   └── DataStoreManager.kt  → User preferences storage
│   └── model/
│       ├── Note.kt              → Note data entity
│       ├── Quiz.kt              → Quiz question model
│       └── Flashcard.kt         → Flashcard model
├── notifications/
│   ├── NotificationHelper.kt    → Creates & shows notifications
│   ├── ReminderWorker.kt        → Background periodic reminders
│   └── AlarmReceiver.kt         → Exact-time alarm handler
├── ui/
│   ├── theme/                   → Material 3 colors, typography, shapes
│   ├── components/              → 6 reusable UI composables
│   ├── navigation/              → Screen routes, NavGraph, Drawer
│   └── screens/                 → 9 screen packages (splash, auth, dashboard, etc.)
└── utils/
    └── Constants.kt             → App-wide constants (API key, subjects, colors)
```

---

## 🔧 Technologies Used & Why

### 1. Jetpack Compose (UI Framework)
**What:** Declarative UI toolkit that replaces XML layouts.
**Why:** Compose uses Kotlin functions (`@Composable`) to build UI. It's reactive — when data changes, the UI automatically recomposes. This eliminates `findViewById()`, Adapters, and XML boilerplate.

**Where used:** Every screen and component in the app.

### 2. Navigation Compose
**What:** Handles screen-to-screen navigation within a single Activity.
**Why:** Instead of multiple Activities with Intents, we use one Activity and swap Composable screens via routes (like `"notes"`, `"quiz/{noteId}"`). This is more memory-efficient and allows smooth transitions.

**Where used:** `NavGraph.kt`, `Screen.kt` (sealed class defining all routes).

```kotlin
// Example: Navigating to a note's quiz
navController.navigate(Screen.Quiz.createRoute(noteId))
// Route pattern: "quiz/{noteId}" → "quiz/5"
```

### 3. Room Database (Local Storage)
**What:** SQLite abstraction layer with compile-time query verification.
**Why:** Notes need to persist even when the app is closed. Room provides:
- `@Entity` → Defines table structure (Note table with id, title, content, subject, etc.)
- `@Dao` → Defines SQL queries as Kotlin functions
- `@Database` → Creates the database singleton

**Where data is stored:** SQLite database file in the app's private storage (`/data/data/com.example.ainote/databases/`).

```kotlin
// Example: Getting all notes as a reactive Flow
@Query("SELECT * FROM notes ORDER BY updatedAt DESC")
fun getAllNotes(): Flow<List<Note>>
```

### 4. DataStore Preferences
**What:** Modern replacement for SharedPreferences.
**Why:** Stores small key-value user settings (username, dark mode toggle, reminder preferences). Unlike SharedPreferences, DataStore is asynchronous and uses Kotlin Coroutines + Flow, preventing UI freezes.

**Where used:** `DataStoreManager.kt` — stores `user_name`, `dark_mode`, `daily_reminder`, `reminder_hour`, `reminder_minute`.

### 5. Kotlin Coroutines & StateFlow
**What:** Coroutines handle background tasks; StateFlow holds reactive state.
**Why:** Database queries and API calls must run off the main thread (otherwise the app freezes). Coroutines make async code look synchronous. StateFlow notifies the UI whenever data changes.

```kotlin
// ViewModel exposes state
private val _notes = MutableStateFlow<List<Note>>(emptyList())
val notes: StateFlow<List<Note>> = _notes

// UI collects state
val notes by viewModel.notes.collectAsState()
```

### 6. Google Generative AI SDK (Gemma 4 31B)
**What:** Google's AI model accessed via API for text generation.
**Why:** Generates intelligent summaries, quiz questions, and flashcards from note content. The `gemma-4-31b-it` model (instruction-tuned) understands prompts and returns structured educational content.

**Where used:** `AIViewModel.kt` and `DocumentViewModel.kt`

### 7. Google ML Kit (OCR Text Recognition)
**What:** On-device machine learning for text extraction.
**Why:** Allows users to upload PDFs or images. The app uses Android's `PdfRenderer` to convert PDF pages to bitmaps, then ML Kit extracts the text so Gemma 4 can summarize it.
**Where used:** `DocumentViewModel.kt`

### 8. WorkManager (Periodic Reminders)
**What:** Schedules guaranteed background work that survives app restarts.
**Why:** Daily study reminders need to fire even if the app is closed. WorkManager handles battery optimization, doze mode, and ensures the reminder eventually runs.

**Where used:** `ReminderWorker.kt` — runs periodically, triggers a notification.

### 8. AlarmManager (Exact-Time Alarms)
**What:** Fires a BroadcastReceiver at an exact time.
**Why:** For one-time study alarms (e.g., "remind me at 3:00 PM"), AlarmManager provides exact timing that WorkManager cannot guarantee.

**Where used:** `ReminderScreen.kt` (schedules), `AlarmReceiver.kt` (receives and shows notification).

### 9. Notifications
**What:** System notifications with channels (required on Android 8+).
**Why:** Alerts the user to study even when the app is in the background. Uses `NotificationChannel` for categorization and `PendingIntent` to open the app when tapped.

**Where used:** `NotificationHelper.kt`

### 10. Material 3 Theming
**What:** Google's design system with dynamic colors, shapes, typography.
**Why:** Provides a consistent, modern UI. Custom theme with indigo/teal palette, rounded shapes, and the Inter font family gives the app a premium look.

**Where used:** `ui/theme/` — `Color.kt`, `Theme.kt`, `Type.kt`.

---

## 📌 UI Components & Why Each Was Built

| Component | Compose Concept Used | Why It Exists |
|-----------|---------------------|---------------|
| `LazyColumn` | Recycling list | Efficiently displays large note lists — only renders visible items (like RecyclerView) |
| `LazyVerticalGrid` | Grid layout | Dashboard feature cards in a 2-column grid layout |
| `HorizontalPager` | Swipeable pages | Flashcards — swipe left/right between cards |
| `ExposedDropdownMenuBox` | Dropdown/Spinner | Subject selection when creating/filtering notes |
| `CustomRatingBar` | Custom composable | Rate note usefulness (1-5 stars) with animated interactions |
| `ModalNavigationDrawer` | Side drawer | App-wide navigation menu (Dashboard, Notes, Reminders, Profile) |
| `TopAppBar` | App bar | Title + back button + action icons on each screen |
| `FloatingActionButton` | FAB | Quick "Add Note" button on the Notes list screen |
| `Card` | Material card | Note cards with color accent, subject tag, rating, and dropdown menu |
| `AlertDialog` | Dialog | Confirmation dialogs (delete note, set reminder) |
| `DatePicker / TimePicker` | Material pickers | Select date and time for study reminders |
| `LinearProgressIndicator` | Progress bar | Shows loading state during AI processing |
| `AnimatedVisibility` | Animations | Smooth show/hide transitions for UI elements |

---

## 📐 Key Android Concepts Demonstrated

### Intents
- **Implicit Intent:** Share note content via `Intent.ACTION_SEND` (share to WhatsApp, email, etc.)
- **PendingIntent:** Used in notifications — opens `MainActivity` when notification is tapped.

```kotlin
// Share Intent in NoteCard.kt
val sendIntent = Intent(Intent.ACTION_SEND).apply {
    putExtra(Intent.EXTRA_TEXT, "${note.title}\n\n${note.content}")
    type = "text/plain"
}
context.startActivity(Intent.createChooser(sendIntent, "Share Note"))
```

### LaunchedEffect
**What:** Runs a side-effect (one-time action) when a composable enters composition.
**Why:** Used in SplashScreen to delay 2 seconds then navigate, and in note screens to load data when the screen opens.

```kotlin
// SplashScreen.kt
LaunchedEffect(Unit) {
    delay(2000)
    onSplashFinished()  // Navigate to Login
}
```

### Single Activity Architecture
**Why:** The entire app runs in one `MainActivity`. Screens are swapped using Navigation Compose. This is the modern Android recommended approach — it's lighter than multiple Activities and allows shared ViewModels.

### Permissions (Declared in AndroidManifest.xml)
| Permission | Why |
|-----------|-----|
| `INTERNET` | API calls to Gemma 4 AI model |
| `POST_NOTIFICATIONS` | Show study reminder notifications |
| `SCHEDULE_EXACT_ALARM` | Fire alarms at exact times |
| `RECORD_AUDIO` | Voice-to-text note input (future) |
| `CAMERA` | OCR text scanning from photos (future) |
| `VIBRATE` | Vibrate on notification |

---

## 💾 Data Flow: How the App Functions

### Creating a Note
```
User types title + content + selects subject + picks color + sets rating
    → AddEditNoteScreen captures input
    → NotesViewModel.saveNote(note) called
    → NoteDao.insertNote(note) writes to Room SQLite database
    → Flow<List<Note>> automatically emits updated list
    → NotesScreen recomposes with new note visible
```

### Scanning a Document (PDF/Image)
```
User selects "Scan Doc" → Chooses PDF or Image
    → DocumentViewModel.processDocument(uri) called
    → [PDF]: PdfRenderer converts pages to Bitmaps
    → [Image/PDF]: ML Kit TextRecognition extracts text from Bitmaps
    → User taps "Generate AI Summary" → Prompt sent to Gemma 4
    → Result shown and user can save it as a new Note in Room DB
```

### Generating AI Summary
```
User opens a note → taps "Summary" button
    → AIViewModel.generateSummary() called
    → Prompt built from note title + subject + content
    → GenerativeModel.generateContent(prompt) → API call to Gemma 4 31B
    → Response text parsed and displayed
    → Summary saved back to the note in Room DB
```

### Setting a Reminder
```
User opens Reminder screen → picks date + time → taps "Set Alarm"
    → AlarmManager.setExactAndAllowWhileIdle() schedules alarm
    → At scheduled time: AlarmReceiver.onReceive() fires
    → NotificationHelper.showStudyReminder() displays notification
    → User taps notification → PendingIntent opens MainActivity
```

### Dark Mode Toggle
```
User toggles switch in Profile screen
    → DataStoreManager.setDarkMode(true) saves to DataStore
    → MainActivity collects isDarkMode Flow
    → AiNoteTheme(darkTheme = true) recomposes entire app in dark colors
```

---

## 📊 Database Schema

### Notes Table
| Column | Type | Description |
|--------|------|-------------|
| id | Int (PK) | Auto-generated primary key |
| title | String | Note title |
| content | String | Note body text |
| subject | String | Category (Math, Science, etc.) |
| summary | String | AI-generated summary (stored for reuse) |
| rating | Int | Usefulness rating (0-5 stars) |
| colorIndex | Int | Index into NOTE_COLORS palette |
| createdAt | Long | Timestamp of creation |
| updatedAt | Long | Timestamp of last modification |

---

## 🚀 Features Summary

| # | Feature | Technology | Unit |
|---|---------|-----------|------|
| 1 | Splash Screen with animation | LaunchedEffect + AnimatedVisibility | I |
| 2 | Progress bar during AI loading | LinearProgressIndicator | I |
| 3 | Subject dropdown/spinner | ExposedDropdownMenuBox | I |
| 4 | Star rating bar | Custom Composable | I |
| 5 | Notes list | LazyColumn | I |
| 6 | Scrollable content | verticalScroll / LazyColumn | I |
| 7 | Navigation drawer menu | ModalNavigationDrawer | I, VI |
| 8 | Share notes (Implicit Intent) | Intent.ACTION_SEND | II |
| 9 | Notification on tap (PendingIntent) | PendingIntent + NotificationCompat | II |
| 10 | Periodic reminders | WorkManager (CoroutineWorker) | II |
| 11 | Exact-time alarms | AlarmManager + BroadcastReceiver | II |
| 12 | Notification channels | NotificationChannel (Android 8+) | III |
| 13 | Date & Time pickers | DatePickerDialog / TimePickerDialog | III |
| 14 | Custom buttons with gradient | Canvas + Brush.horizontalGradient | IV |
| 15 | Custom text fields | OutlinedTextField with animations | IV |
| 16 | Material 3 theming | Custom ColorScheme + Typography | IV |
| 17 | DataStore preferences | DataStore<Preferences> | V |
| 18 | Runtime permissions declared | AndroidManifest.xml | V |
| 19 | Flashcard swipe (HorizontalPager) | HorizontalPager + flip animation | VI |
| 20 | Single Activity navigation | NavHost + composable() routes | VI |
| 21 | AI Summary generation | Google Generative AI (Gemma 4 31B) | AI |
| 22 | AI Quiz generation | Structured prompts + parsing | AI |
| 23 | AI Flashcard generation | Structured prompts + parsing | AI |
| 24 | Dashboard with grid layout | LazyVerticalGrid | I |
| 25 | Dark mode toggle | DataStore + dynamic theming | IV, V |

---

## 🛠 How to Run

1. Open project in **Android Studio Ladybug** or later
2. Sync Gradle (File → Sync Project with Gradle Files)
3. Connect device or start emulator (min API 26 / Android 8.0)
4. Click **Run ▶**
5. The app launches with a splash screen → Login → Dashboard

---

## 📦 Dependencies

| Library | Purpose |
|---------|---------|
| `androidx.compose.*` | Jetpack Compose UI framework |
| `androidx.navigation:navigation-compose` | Screen navigation |
| `androidx.room:room-*` | Local SQLite database |
| `androidx.datastore:datastore-preferences` | Key-value preferences |
| `androidx.work:work-runtime-ktx` | Background task scheduling |
| `com.google.ai.client.generativeai` | Gemma 4 AI API |
| `com.google.mlkit:text-recognition` | OCR (future feature) |
| `androidx.compose.material:material-icons-extended` | Extended icon set |
| `com.google.accompanist:accompanist-permissions` | Runtime permissions |
| `com.google.code.gson:gson` | JSON serialization |

---

## 👨‍🎓 Author

Built as a comprehensive Android development project demonstrating modern Android architecture, Jetpack Compose UI, AI integration, local data persistence, background scheduling, and Material 3 design principles.
