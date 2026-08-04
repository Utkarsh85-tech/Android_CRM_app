// REPLACE TaskNavHost.kt entirely
// app/src/main/java/com/example/nexoworxcrmapp/ui/task/TaskNavHost.kt

package com.example.nexoworxcrmapp.ui.task

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TaskNavHost(
    modifier: Modifier = Modifier,
    openCreateOnLaunch: Boolean = false,
    onCreateHandled: () -> Unit = {},
    pendingVoiceDraft: com.example.nexoworxcrmapp.speech.TaskDraft? = null,
    onVoiceDraftConsumed: () -> Unit = {},
) {
    // No parentId = loads all tasks, shows category filter
    val taskViewModel: TaskViewModel = viewModel()

    // Deep-link support: Home's Quick Create can open the create sheet
    // directly without Home needing to know how TaskScreen works internally.
    androidx.compose.runtime.LaunchedEffect(openCreateOnLaunch) {
        if (openCreateOnLaunch) {
            taskViewModel.openCreateSheet()
            onCreateHandled()
        }
    }
    androidx.compose.runtime.LaunchedEffect(pendingVoiceDraft) {
        if (pendingVoiceDraft != null) {
            taskViewModel.openCreateSheetWithPrefill(pendingVoiceDraft)
            onVoiceDraftConsumed()
        }
    }

    TaskScreen(
        modifier = modifier,
        showHeader = true,
        showCategoryFilter = true,
        viewModel = taskViewModel,
    )
}
