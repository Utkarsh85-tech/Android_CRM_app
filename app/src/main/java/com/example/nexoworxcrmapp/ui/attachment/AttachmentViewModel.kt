package com.example.nexoworxcrmapp.ui.attachment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.attachment.AttachmentItem
import com.example.nexoworxcrmapp.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L // 5MB

data class AttachmentUiState(
    val isLoading: Boolean = false,
    val attachments: List<AttachmentItem> = emptyList(),
    val pendingIds: Set<String> = emptySet(),
    val failedIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val fileSizeWarning: Boolean = false,
    val pendingFileBytes: ByteArray? = null,
    val pendingFileName: String? = null,
)

class AttachmentViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val recordId: String = checkNotNull(savedStateHandle["recordId"])
    private val repo = NetworkModule.attachmentRepository

    private val _uiState = MutableStateFlow(AttachmentUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                repo.observeAttachments(recordId),
                NetworkModule.syncStatusRepository.observePendingIds(),
                NetworkModule.syncStatusRepository.observeFailedIds(),
            ) { list, pending, failed -> Triple(list, pending, failed) }
                .collect { (list, pending, failed) ->
                    _uiState.update {
                        it.copy(isLoading = false, attachments = list, pendingIds = pending, failedIds = failed)
                    }
                }
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repo.pullForParent(recordId) // no-op gracefully if offline; cached list still shows
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onFilePicked(fileName: String, fileBytes: ByteArray) {
        if (fileBytes.size > MAX_FILE_SIZE_BYTES) {
            _uiState.update { it.copy(fileSizeWarning = true, pendingFileBytes = fileBytes, pendingFileName = fileName) }
        } else {
            queueUpload(fileName, fileBytes)
        }
    }

    fun confirmLargeUpload() {
        val bytes = _uiState.value.pendingFileBytes ?: return
        val name = _uiState.value.pendingFileName ?: return
        _uiState.update { it.copy(fileSizeWarning = false, pendingFileBytes = null, pendingFileName = null) }
        queueUpload(name, bytes)
    }

    fun dismissSizeWarning() {
        _uiState.update { it.copy(fileSizeWarning = false, pendingFileBytes = null, pendingFileName = null) }
    }

    private fun queueUpload(fileName: String, fileBytes: ByteArray) {
        viewModelScope.launch { repo.addAttachment(recordId, fileName, fileBytes) }
    }

    fun deleteFile(contentDocumentId: String) {
        viewModelScope.launch { repo.deleteAttachment(contentDocumentId) }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}