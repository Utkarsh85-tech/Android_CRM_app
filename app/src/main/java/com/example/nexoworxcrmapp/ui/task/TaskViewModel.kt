package com.example.nexoworxcrmapp.ui.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.data.Task
import com.example.nexoworxcrmapp.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Category filter for the Tasks tab
enum class TaskCategory { All, Leads, Accounts, Opportunities }

data class TaskUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val errorMessage: String? = null,
    val selectedCategory: TaskCategory = TaskCategory.All,
    // For edit/create sheet
    val showSheet: Boolean = false,
    val editingTask: Task? = null,
    val sheetSubject: String = "",
    val sheetStatus: String = "Not Started",
    val sheetPriority: String = "Normal",
    val sheetDueDate: String = "",
    val sheetDescription: String = "",
    val sheetSaving: Boolean = false,
    val sheetError: String? = null,
    // Delete confirmation
    val deletingTaskId: String? = null,
)

class TaskViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val parentId: String? = savedStateHandle.get<String>("parentId")
        ?.takeIf { it != "null" && it.isNotBlank() }

    private val isLead: Boolean = savedStateHandle.get<Boolean>("isLead") ?: false

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        // Observe the repository's task flow so the UI updates automatically
        viewModelScope.launch {
            CrmRepository.tasks.collect { allTasks ->
                _uiState.update { state ->
                    val filtered = if (parentId != null) {
                        allTasks.filter { (isLead && it.whoId == parentId) || (!isLead && it.whatId == parentId) }
                    } else {
                        allTasks
                    }
                    state.copy(tasks = filtered)
                }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = if (parentId != null) {
                CrmRepository.refreshTasksForParent(parentId, isLead)
            } else {
                CrmRepository.refreshTasks()
            }
            // Just update loading state; the list itself comes from the collect block above
            _uiState.update { it.copy(isLoading = false, errorMessage = (result as? ApiResult.Error)?.message) }
        }
    }

    fun selectCategory(category: TaskCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun filteredTasks(): List<Task> {
        val all = _uiState.value.tasks
        val leads = CrmRepository.leads.value.map { it.id }.toSet()
        val accounts = CrmRepository.accounts.value.map { it.id }.toSet()
        val opportunities = CrmRepository.opportunities.value.map { it.id }.toSet()

        return when (_uiState.value.selectedCategory) {
            TaskCategory.All -> all
            TaskCategory.Leads -> all.filter { it.whoId.isNotBlank() && it.whoId in leads }
            TaskCategory.Accounts -> all.filter { it.whatId.isNotBlank() && it.whatId in accounts }
            TaskCategory.Opportunities -> all.filter { it.whatId.isNotBlank() && it.whatId in opportunities }
        }
    }

    fun parentNameForTask(task: Task): String {
        if (task.whoId.isNotBlank()) {
            return CrmRepository.leads.value.find { it.id == task.whoId }?.fullName ?: ""
        }
        if (task.whatId.isNotBlank()) {
            CrmRepository.accounts.value.find { it.id == task.whatId }?.name?.let { return it }
            CrmRepository.opportunities.value.find { it.id == task.whatId }?.name?.let { return it }
        }
        return ""
    }

    fun openCreateSheet() {
        _uiState.update {
            it.copy(
                showSheet = true, editingTask = null,
                sheetSubject = "", sheetStatus = "Not Started",
                sheetPriority = "Normal", sheetDueDate = "",
                sheetDescription = "", sheetError = null,
            )
        }
    }

    fun openCreateSheetWithPrefill(draft: com.example.nexoworxcrmapp.speech.TaskDraft) {
        _uiState.update {
            it.copy(
                showSheet = true, editingTask = null,
                sheetSubject = draft.subject, sheetStatus = "Not Started",
                sheetPriority = draft.priority, sheetDueDate = draft.dueDate,
                sheetDescription = draft.description, sheetError = null,
            )
        }
    }

    fun openEditSheet(task: Task) {
        _uiState.update {
            it.copy(
                showSheet = true, editingTask = task,
                sheetSubject = task.subject, sheetStatus = task.status,
                sheetPriority = task.priority, sheetDueDate = task.dueDate,
                sheetDescription = task.description, sheetError = null,
            )
        }
    }

    fun closeSheet() = _uiState.update { it.copy(showSheet = false) }
    fun onSubjectChange(v: String) = _uiState.update { it.copy(sheetSubject = v) }
    fun onStatusChange(v: String) = _uiState.update { it.copy(sheetStatus = v) }
    fun onPriorityChange(v: String) = _uiState.update { it.copy(sheetPriority = v) }
    fun onDueDateChange(v: String) = _uiState.update { it.copy(sheetDueDate = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(sheetDescription = v) }

    fun saveTask() {
        val s = _uiState.value
        if (s.sheetSubject.isBlank()) {
            _uiState.update { it.copy(sheetError = "Subject is required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(sheetSaving = true, sheetError = null) }
            try {
                if (s.editingTask == null) {
                    CrmRepository.createTask(
                        subject = s.sheetSubject, status = s.sheetStatus,
                        priority = s.sheetPriority, dueDate = s.sheetDueDate.takeIf { it.isNotBlank() },
                        whoId = if (isLead) parentId else null,
                        whatId = if (!isLead) parentId else null,
                        description = s.sheetDescription.takeIf { it.isNotBlank() },
                    )
                } else {
                    CrmRepository.updateTask(
                        id = s.editingTask.id, subject = s.sheetSubject, status = s.sheetStatus,
                        priority = s.sheetPriority, dueDate = s.sheetDueDate.takeIf { it.isNotBlank() },
                        whoId = if (isLead) parentId else s.editingTask.whoId.takeIf { it.isNotBlank() },
                        whatId = if (!isLead) parentId else s.editingTask.whatId.takeIf { it.isNotBlank() },
                        description = s.sheetDescription.takeIf { it.isNotBlank() },
                    )
                }
                _uiState.update { it.copy(sheetSaving = false, showSheet = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(sheetSaving = false, sheetError = e.message ?: "Save failed") }
            }
        }
    }

    fun requestDelete(taskId: String) = _uiState.update { it.copy(deletingTaskId = taskId) }
    fun cancelDelete() = _uiState.update { it.copy(deletingTaskId = null) }

    fun confirmDelete() {
        val id = _uiState.value.deletingTaskId ?: return
        viewModelScope.launch {
            try {
                CrmRepository.deleteTask(id)
                _uiState.update { it.copy(deletingTaskId = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(deletingTaskId = null, errorMessage = "Delete failed") }
            }
        }
    }
}