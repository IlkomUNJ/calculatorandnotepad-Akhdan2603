package com.example.training

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

interface NoteStorageService {
    fun saveNote(note: NoteItem)
    fun getNoteById(id: String): NoteItem?
    fun getAllNotes(): List<NoteItem>
    fun deleteNote(id: String)
}

data class NoteItem(
    val id: String,
    var title: String,
    var content: String,
    val snippet: String,
    val lastModified: String
)

enum class EditorMode {
    LIST_VIEW,
    EDIT_VIEW
}

object DummyNoteStorageService : NoteStorageService {
    private val notesMap = mutableMapOf<String, NoteItem>()

    init {
        notesMap["id-1"] = NoteItem("id-1", "Resep Kue Cokelat", "Tepung, gula, 3 butir telur...", "Tepung, gula, 3 butir telur...", "2 jam lalu")
    }

    override fun saveNote(note: NoteItem) { notesMap[note.id] = note }
    override fun getNoteById(id: String): NoteItem? = notesMap[id]
    override fun getAllNotes(): List<NoteItem> = notesMap.values.toList().sortedByDescending { it.lastModified }
    override fun deleteNote(id: String) { notesMap.remove(id) }
}

class TextEditorViewModel(private val storageService: NoteStorageService = DummyNoteStorageService) : ViewModel() {
    var currentMode by mutableStateOf(EditorMode.LIST_VIEW)
        private set
    var notes by mutableStateOf<List<NoteItem>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var message by mutableStateOf("")
        private set

    var isDark by mutableStateOf(false)
        private set

    var currentNoteId by mutableStateOf<String?>(null)
    var editorContent by mutableStateOf("")
        private set
    var editorTitle by mutableStateOf("Catatan Baru")
        private set

    init {
        loadNotes()
    }

    fun toggleTheme() {
        isDark = !isDark
    }

    fun loadNotes() {
        if (isLoading) return
        isLoading = true
        message = ""

        viewModelScope.launch {
            delay(500)
            notes = storageService.getAllNotes()
            isLoading = false
        }
    }

    fun saveContent() {
        val isNewNote = currentNoteId == null
        val newId = currentNoteId ?: UUID.randomUUID().toString()
        val timestamp = "Baru saja"

        val noteToSave = NoteItem(
            id = newId,
            title = editorTitle.ifBlank { "Catatan Tanpa Judul" },
            content = editorContent,
            snippet = editorContent.take(50) + if (editorContent.length > 50) "..." else "",
            lastModified = timestamp
        )

        storageService.saveNote(noteToSave)

        currentNoteId = newId
        message = if (isNewNote) "Catatan berhasil dibuat dan disimpan!" else "Catatan berhasil diperbarui!"
    }

    fun enterNewNoteMode() {
        currentNoteId = null
        editorTitle = "Catatan Baru"
        editorContent = ""
        currentMode = EditorMode.EDIT_VIEW
    }

    fun openNote(noteId: String) {
        val fullNote = storageService.getNoteById(noteId)

        currentNoteId = noteId
        editorTitle = fullNote?.title ?: "Catatan Tidak Ditemukan"
        editorContent = fullNote?.content ?: "Konten tidak dapat dimuat."
        currentMode = EditorMode.EDIT_VIEW
    }

    fun exitEditorMode() {
        currentMode = EditorMode.LIST_VIEW
        loadNotes()
    }

    fun updateContent(newContent: String) { editorContent = newContent }
    fun updateTitle(newTitle: String) { editorTitle = newTitle }
    fun copyContent() { if (editorContent.isNotEmpty()) message = "Teks berhasil disalin!" else message = "Tidak ada teks untuk disalin." }
    fun clearMessage() { message = "" }
}