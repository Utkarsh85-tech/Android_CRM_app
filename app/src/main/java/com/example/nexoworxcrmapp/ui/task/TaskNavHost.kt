// REPLACE TaskNavHost.kt entirely
// app/src/main/java/com/example/nexoworxcrmapp/ui/task/TaskNavHost.kt

package com.example.nexoworxcrmapp.ui.task

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TaskNavHost(modifier: Modifier = Modifier) {
    // No parentId = loads all tasks, shows category filter
    TaskScreen(
        modifier = modifier,
        showHeader = true,
        showCategoryFilter = true,
        viewModel = viewModel(),
    )
}
