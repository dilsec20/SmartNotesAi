# 📘 SmartNotes AI – Advanced Study Assistant

## 📱 What This App Does

SmartNotes AI is an **AI-powered Android study assistant** designed to supercharge your learning. It provides a comprehensive suite of educational tools, allowing users to create notes, scan documents via OCR, generate AI summaries, create flashcards, set study reminders, and generate multiple-choice questions (MCQs) for self-assessment — all within a beautifully designed Jetpack Compose application.

---

## 🏗 Architecture & Structure

The project follows a modern **MVVM (Model-View-ViewModel)** architecture and Clean Architecture principles, utilizing a structured multi-package approach for scalability and maintainability:

- **`data/`**: Contains the Room Database setup, DAOs (`NoteDao`), DataStore managers, and data models (`Note`, `Flashcard`, `Quiz`).
- **`ui/`**: Houses all Jetpack Compose UI elements.
  - **`screens/`**: Organized by feature (`dashboard`, `notes`, `flashcards`, `quiz`, `scanner`, `summary`, `reminder`, `auth`, `profile`).
  - **`components/`**: Reusable custom UI widgets (`NoteCard`, `CustomButton`, etc.).
  - **`navigation/`**: Centralized routing using Jetpack Navigation Compose (`NavGraph`, `AppDrawer`).
  - **`theme/`**: Color palettes, typography, and styling.
- **`notifications/`**: Manages local study reminders and alarms using Android WorkManager and AlarmManager (`ReminderWorker`, `AlarmReceiver`).
- **`utils/`**: Helper classes and constants.

```text
View (Compose Screens) → ViewModel → Model (Room DB / Gemini API / ML Kit)
```

---

## 🔧 Technologies Used

### 1. Jetpack Compose (UI Framework)
Modern declarative UI toolkit used for all screens and components. Provides a fluid, dynamic, and state-driven user experience.

### 2. Room Database & DataStore (Local Storage)
Room provides type-safe SQLite access for persisting Notes, Flashcards, and Quizzes offline. Preferences DataStore is used for managing user session/preferences.

### 3. Google Generative AI SDK (Gemini 1.5 Flash)
Analyzes user notes and scanned text to provide intelligent summaries and generate custom multiple-choice quizzes.

### 4. Google ML Kit (OCR Text Recognition)
Powers the built-in Document Scanner. Extracts text from captured images and PDFs entirely on-device.

### 5. WorkManager & AlarmManager
Used to schedule and deliver reliable local push notifications for study reminders.

---

## 🚀 Key Features

1. **Dashboard & Notes Management**: Create, organize, and manage your text notes.
2. **Document Scanner**: Scan physical documents or import PDFs to extract text instantly using OCR.
3. **AI Summarizer**: Leverage Gemini AI to condense long notes or scanned documents into concise study summaries.
4. **Interactive Quizzes (MCQs)**: Automatically generate customized multiple-choice quizzes based on your study materials.
5. **Flashcards**: Create and review flashcards for active recall and spaced repetition learning.
6. **Study Reminders**: Set specific times and schedules to get notified to study.
7. **Authentication & Profile**: Manage your user profile and personalization securely.

---

## 🎨 UI & Styling Concepts Used (Study Guide)

To help explain how the UI is built under the hood, here are the core **Jetpack Compose** styling and layout concepts used heavily throughout this project:

### 1. `LazyColumn` & `LazyVerticalGrid`
Instead of older `RecyclerViews`, this app uses Compose's "Lazy" components to render large lists efficiently:
- **`LazyColumn`**: Used in screens like the **Notes List** or **Quiz Screen** to create smooth vertical scrolling lists. It only renders items currently visible on the screen, significantly saving memory and improving performance.
- **`LazyVerticalGrid`**: Used to display items in a responsive grid layout. For example, the **Dashboard** option tiles or the **Flashcards** grid layout use `GridCells.Fixed(2)` to create a perfect two-column grid.

### 2. State Hoisting & Management (`remember` & `StateFlow`)
State management in this app relies on the declarative nature of Compose. The UI automatically updates whenever the state changes.
- **`remember { mutableStateOf(...) }`**: Used to cache state directly inside a composable so it isn't lost during recomposition (e.g., keeping track of text typed in a `CustomTextField` or whether a dropdown menu is open).
- **`StateFlow`**: Real-time variables from the `ViewModel` are observed in the UI using `collectAsState()`. This allows the screen to instantly react and re-draw when backend data changes (like a new note appearing in the Room database).

### 3. Material Design 3 (`Scaffold`, `Card`, `Surface`)
We heavily utilize Google's Material Design 3 components for beautiful, consistent UI elements out of the box:
- **`Scaffold`**: Wraps entire screens to automatically provide structured layout slots for top app bars, floating action buttons (`FAB`), and navigation drawers (`AppDrawer`).
- **`Card` / `ElevatedCard`**: Used to beautifully containerize individual Notes and Flashcards, giving them visual hierarchy, slight drop-shadows (`elevation`), and rounded edges.
- **`Surface`**: Acts as a background container that automatically handles text color contrasts and clipping constraints.

### 4. The `Modifier` System
Styling in Compose completely replaces old XML layout attributes by chaining `Modifier` functions:
- `Modifier.fillMaxSize()` or `.fillMaxWidth()` to handle sizing.
- `Modifier.padding(16.dp)` to space elements dynamically without hardcoded margins.
- `Modifier.clip(RoundedCornerShape(12.dp))` to round the corners of images, buttons, or custom containers.
- `Modifier.clickable { ... }` to make any standard UI text or box instantly act like an interactive, tappable button with a ripple effect.

---

## 🏃 How to Run

1. Clone the repository: `git clone https://github.com/dilsec20/SmartNotesAi.git`
2. Open the project in **Android Studio Ladybug** (or later).
3. Ensure you have the required API keys (e.g., Gemini API key) configured in your `local.properties` or environment if needed.
4. Sync Gradle (File → Sync Project with Gradle Files).
5. Connect a physical device or start an emulator (Min API 26+).
6. Click **Run ▶** and start learning smarter!
