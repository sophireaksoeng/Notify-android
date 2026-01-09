package com.team.notify.taskflow.presentation.notes

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
import com.team.notify.data.sync.FirebaseSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NotesPageViewModel @Inject constructor(
    private val repo: PageRepository,
    private val firebaseSyncService: FirebaseSyncService
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _contentValue = MutableStateFlow(TextFieldValue(""))
    val contentValue = _contentValue.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving = _saving.asStateFlow()

    private val _lastEdited = MutableStateFlow("just now")
    val lastEdited = _lastEdited.asStateFlow()

    private var autoSaveJob: Job? = null

    // Track ID and Space
    private var currentPageId: String? = null
    private var currentSpaceId: String = ""

    // FIXED: load now accepts spaceId to handle new pages correctly
    fun load(pageId: String?, spaceId: String) {
        this.currentSpaceId = spaceId

        if (pageId.isNullOrBlank()) {
            // Initialize new page
            this.currentPageId = UUID.randomUUID().toString()
            _title.value = ""
            _contentValue.value = TextFieldValue("")
            return
        }

        this.currentPageId = pageId
        viewModelScope.launch {
            repo.getPageById(pageId).collect { page ->
                if (page != null) {
                    _title.value = page.title
                    _contentValue.update { current ->
                        if (current.text != page.content) {
                            TextFieldValue(text = page.content ?: "")
                        } else {
                            current
                        }
                    }
                    // Sync spaceId from DB just in case
                    page.spaceId?.let { currentSpaceId = it }
                    _lastEdited.value = "recently"
                }
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
        triggerAutoSave()
    }

    fun updateContentValue(newValue: TextFieldValue) {
        _contentValue.value = newValue
        triggerAutoSave()
    }

    fun applyStyle(style: SpanStyle) {
        val current = _contentValue.value
        val selection = current.selection
        if (selection.collapsed) return

        val builder = AnnotatedString.Builder(current.annotatedString)
        builder.addStyle(style, selection.start, selection.end)
        _contentValue.value = current.copy(annotatedString = builder.toAnnotatedString())
        triggerAutoSave()
    }

    fun insertBulletPoint() {
        val current = _contentValue.value
        val cursor = current.selection.start.coerceAtLeast(0)
        val insertText = "\n• "
        val newText = StringBuilder(current.text).insert(cursor, insertText).toString()
        _contentValue.value = TextFieldValue(
            text = newText,
            selection = TextRange(cursor + insertText.length)
        )
        triggerAutoSave()
    }

    private fun triggerAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            _saving.value = true
            delay(1500)
            performSave()
            _saving.value = false
            _lastEdited.value = "just now"
        }
    }

    private suspend fun performSave() {
        val id = currentPageId ?: return
        val currentTitle = _title.value
        val currentContent = _contentValue.value.text
        val now = System.currentTimeMillis()

        try {
            val existingPage = repo.getPageById(id).firstOrNull()

            val pageToSave = if (existingPage == null) {
                PageEntity(
                    id = id,
                    spaceId = currentSpaceId,
                    title = currentTitle,
                    content = currentContent,
                    createdAt = now,
                    updatedAt = now,
                    version = 1
                )
            } else {
                // UPDATE EXISTING
                existingPage.copy(
                    title = currentTitle,
                    content = currentContent,
                    updatedAt = now,
                    version = existingPage.version + 1
                )
            }

            // Save to Local DB
            if (existingPage == null) {
                repo.insert(pageToSave)
            } else {
                repo.update(pageToSave)
            }

            // Sync to Firebase
            try {
                firebaseSyncService.updatePageInFirebase(pageToSave)
            } catch (e: Exception) {
                // Handle sync error
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}