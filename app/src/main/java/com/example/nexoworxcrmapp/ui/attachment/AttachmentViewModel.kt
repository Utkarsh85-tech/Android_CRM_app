package com.example.nexoworxcrmapp.ui.attachment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.attachment.AttachmentItem
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L // 5MB

data class AttachmentUiState(
    val isLoading: Boolean = false,
    val attachments: List<AttachmentItem> = emptyList(),
    val errorMessage: String? = null,
    val isUploading: Boolean = false,
    val uploadError: String? = null,
    val uploadSuccess: Boolean = false,
    val fileSizeWarning: Boolean = false,
    val pendingFileBytes: ByteArray? = null,
    val pendingFileName: String? = null,
)

class AttachmentViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val recordId: String = checkNotNull(savedStateHandle["recordId"])
    private val repo = NetworkModule.attachmentRepository

    private val _uiState = MutableStateFlow(AttachmentUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val r = repo.fetchAttachments(recordId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, attachments = r.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = r.message)
                }
            }
        }
    }

    /** Call this when user picks a file — checks size before uploading */
    fun onFilePicked(fileName: String, fileBytes: ByteArray) {
        if (fileBytes.size > MAX_FILE_SIZE_BYTES) {
            _uiState.update {
                it.copy(
                    fileSizeWarning = true,
                    pendingFileBytes = fileBytes,
                    pendingFileName = fileName,
                )
            }
        } else {
            uploadFile(fileName, fileBytes)
        }
    }

    /** User confirmed upload despite size warning */
    fun confirmLargeUpload() {
        val bytes = _uiState.value.pendingFileBytes ?: return
        val name = _uiState.value.pendingFileName ?: return
        _uiState.update { it.copy(fileSizeWarning = false, pendingFileBytes = null, pendingFileName = null) }
        uploadFile(name, bytes)
    }

    fun dismissSizeWarning() {
        _uiState.update { it.copy(fileSizeWarning = false, pendingFileBytes = null, pendingFileName = null) }
    }

    private fun uploadFile(fileName: String, fileBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadError = null) }
            when (val r = repo.uploadFile(recordId, fileName, fileBytes)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isUploading = false, uploadSuccess = true) }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isUploading = false, uploadError = r.message)
                }
            }
        }
    }

    fun deleteFile(contentDocumentId: String) {
        viewModelScope.launch {
            when (val r = repo.deleteFile(contentDocumentId)) {
                is ApiResult.Success -> load()
                is ApiResult.Error -> _uiState.update { it.copy(errorMessage = r.message) }
            }
        }
    }

    fun clearUploadError() = _uiState.update { it.copy(uploadError = null) }
    fun clearUploadSuccess() = _uiState.update { it.copy(uploadSuccess = false) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}