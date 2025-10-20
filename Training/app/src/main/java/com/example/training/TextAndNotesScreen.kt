package com.example.training

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TextAndNotesScreen(
    viewModel: TextEditorViewModel = viewModel(),
    onBackToMainMenu: () -> Unit,
    onBackToHome: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val mode by viewModel::currentMode
    val notes by viewModel::notes
    val isLoading by viewModel::isLoading
    val message by viewModel::message

    val content by viewModel::editorContent
    val title by viewModel::editorTitle
    val isDark by viewModel::isDark

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            NotesAndEditorTopBar(
                mode = mode,
                title = if (mode == EditorMode.EDIT_VIEW) title else "Daftar Catatan",
                content = content,
                onBack = {
                    if (mode == EditorMode.EDIT_VIEW) viewModel.exitEditorMode() else onBackToHome()
                },
                onMainMenuClick = onBackToMainMenu,
                onSave = viewModel::saveContent,
                onCopy = viewModel::copyContent,
                onToggleTheme = viewModel::toggleTheme,
                isDark = isDark
            )
        },
        floatingActionButton = {
            if (mode == EditorMode.LIST_VIEW) {
                FloatingActionButton(onClick = viewModel::enterNewNoteMode) {
                    Icon(Icons.Filled.Add, contentDescription = "Buat Catatan Baru")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (mode) {
                EditorMode.LIST_VIEW -> NotesListContent(
                    notes = notes,
                    isLoading = isLoading,
                    onNoteClick = viewModel::openNote,
                    onLoadNotes = viewModel::loadNotes
                )
                EditorMode.EDIT_VIEW -> EditorContent(
                    title = title,
                    content = content,
                    onTitleChange = viewModel::updateTitle,
                    onContentChange = viewModel::updateContent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesAndEditorTopBar(
    mode: EditorMode, title: String, content: String, onBack: () -> Unit,
    onMainMenuClick: () -> Unit, onSave: () -> Unit, onCopy: () -> Unit,
    onToggleTheme: () -> Unit,
    isDark: Boolean
) {
    val clipboardManager = LocalClipboardManager.current
    val themeLabel = if (isDark) "LIGHT" else "DARK"

    TopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali") }
        },
        actions = {
            if (mode == EditorMode.EDIT_VIEW) {
                IconButton(onClick = { clipboardManager.setText(AnnotatedString(content)); onCopy() }) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_share),
                        contentDescription = "Salin"
                    )
                }
                IconButton(onClick = onSave) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_save),
                        contentDescription = "Simpan"
                    )
                }
            }
            IconButton(onClick = onMainMenuClick) {
                Icon(Icons.Filled.Home, contentDescription = "Menu Utama")
            }

            TextButton(
                onClick = onToggleTheme,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(themeLabel, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun NotesListContent(notes: List<NoteItem>, isLoading: Boolean, onNoteClick: (String) -> Unit, onLoadNotes: () -> Unit) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (notes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Tidak ada catatan. Klik '+' untuk membuat yang baru.", modifier = Modifier.padding(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(notes) { note ->
                NoteListItem(note = note, onClick = { onNoteClick(note.id) })
                Divider()
            }
        }
    }
}

@Composable
fun NoteListItem(note: NoteItem, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp, horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = note.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = note.snippet, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = note.lastModified, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun EditorContent(
    title: String, content: String, onTitleChange: (String) -> Unit, onContentChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = title, onValueChange = onTitleChange, modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp), placeholder = { Text("Judul Catatan") },
            textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, disabledContainerColor = MaterialTheme.colorScheme.surface, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
        )
        TextField(
            value = content, onValueChange = onContentChange, modifier = Modifier.fillMaxSize(), label = { Text("Tulis teks Anda di sini...") },
            colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
        )
    }
}