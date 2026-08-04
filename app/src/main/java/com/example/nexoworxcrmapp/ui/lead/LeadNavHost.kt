package com.example.nexoworxcrmapp.ui.lead

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nexoworxcrmapp.speech.LeadDraft
import com.example.nexoworxcrmapp.ui.email.EmailScreen
import com.example.nexoworxcrmapp.ui.task.TaskScreen
import com.example.nexoworxcrmapp.ui.task.TaskViewModel
import com.example.nexoworxcrmapp.ui.attachment.AttachmentScreen
import com.example.nexoworxcrmapp.ui.attachment.AttachmentViewModel
private object LeadRoutes {
    const val LIST = "leads"
    const val CREATE = "lead_create"
    const val DETAIL = "lead_detail/{leadId}"
    const val EDIT = "lead_edit/{leadId}"

    fun detail(leadId: String) = "lead_detail/$leadId"
    fun edit(leadId: String) = "lead_edit/$leadId"
}

@Composable
fun LeadNavHost(
    modifier: Modifier = Modifier,
    pendingVoiceDraft: LeadDraft? = null,
    onVoiceDraftConsumed: () -> Unit = {},
    openCreateOnLaunch: Boolean = false,
    onCreateHandled: () -> Unit = {},
    initialLeadId: String? = null,
    onInitialLeadHandled: () -> Unit = {},
) {
    val navController = rememberNavController()
    var voiceDraftForCreate by remember { mutableStateOf<LeadDraft?>(null) }

    LaunchedEffect(pendingVoiceDraft) {
        if (pendingVoiceDraft != null) {
            voiceDraftForCreate = pendingVoiceDraft
            navController.navigate(LeadRoutes.CREATE) {
                launchSingleTop = true
            }
            onVoiceDraftConsumed()
        }
    }

    // Deep-link support: Home's Quick Create / Search / Notifications can
    // jump straight into this NavHost without knowing its internal routes.
    LaunchedEffect(openCreateOnLaunch) {
        if (openCreateOnLaunch) {
            voiceDraftForCreate = null
            navController.navigate(LeadRoutes.CREATE) { launchSingleTop = true }
            onCreateHandled()
        }
    }
    LaunchedEffect(initialLeadId) {
        if (initialLeadId != null) {
            navController.navigate(LeadRoutes.detail(initialLeadId)) { launchSingleTop = true }
            onInitialLeadHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = LeadRoutes.LIST,
        modifier = modifier,
    ) {
        composable(
            route = "lead_tasks/{leadId}",
            arguments = listOf(navArgument("leadId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("leadId") ?: return@composable
            com.example.nexoworxcrmapp.ui.task.TaskScreen(
                showHeader = false,
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    key = "tasks_$id",
                ),
            )
        }

        composable(
            route = "lead_email/{leadId}/{leadEmail}/{leadName}",
            arguments = listOf(
                navArgument("leadId") { type = NavType.StringType },
                navArgument("leadEmail") { type = NavType.StringType },
                navArgument("leadName") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val leadId = backStackEntry.arguments?.getString("leadId").orEmpty()
            val leadEmail = backStackEntry.arguments?.getString("leadEmail").orEmpty()
            val leadName = backStackEntry.arguments?.getString("leadName").orEmpty()
            EmailScreen(
                leadId = leadId,
                toEmail = leadEmail,
                leadName = leadName,
                onBack = { navController.popBackStack() },
                onSent = { navController.popBackStack() },
            )
        }




        composable(LeadRoutes.LIST) { backStackEntry ->
            val refresh by backStackEntry.savedStateHandle
                .getStateFlow("refreshList", false)
                .collectAsState()

            LeadScreen(
                refreshTrigger = refresh,
                onLeadClick = { lead ->
                    navController.navigate(LeadRoutes.detail(lead.id))
                },
                onAddLeadClick = {
                    voiceDraftForCreate = null
                    navController.navigate(LeadRoutes.CREATE)
                },
            )
        }

        composable(LeadRoutes.CREATE) {
            val draft = voiceDraftForCreate
            LeadEditScreen(
                isCreateMode = true,
                voicePrefill = draft,
                onClose = {
                    voiceDraftForCreate = null
                    navController.popBackStack()
                },
                onSaved = {
                    voiceDraftForCreate = null
                    navController.getBackStackEntry(LeadRoutes.LIST)
                        .savedStateHandle["refreshList"] = true
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = LeadRoutes.DETAIL,
            arguments = listOf(navArgument("leadId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val leadId = backStackEntry.arguments?.getString("leadId").orEmpty()
            val refresh by backStackEntry.savedStateHandle
                .getStateFlow("refresh", false)
                .collectAsState()
            LeadDetailScreen(
                refreshTrigger = refresh,
                onRefreshConsumed = {
                    backStackEntry.savedStateHandle["refresh"] = false
                },
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(LeadRoutes.edit(leadId)) },
                onAddTask = { navController.navigate("lead_task_create/$leadId/true") },
                onEmailClick = { email, name ->
                    navController.navigate("lead_email/$leadId/$email/$name")
                },
                onFilesClick = { navController.navigate("lead_files/$leadId") },
            )


        }

        composable(
            route = LeadRoutes.EDIT,
            arguments = listOf(navArgument("leadId") { type = NavType.StringType }),
        ) {
            LeadEditScreen(
                onClose = { navController.popBackStack() },
                onSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh", true)
                    navController.popBackStack()
                },
            )
        }
        composable(
            route = "lead_tasks/{parentId}/{isLead}",
            arguments = listOf(
                navArgument("parentId") { type = NavType.StringType },
                navArgument("isLead") { type = NavType.BoolType },
            ),
        ) { backStackEntry ->
            val taskViewModel: TaskViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                viewModelStoreOwner = backStackEntry,
            )
            TaskScreen(
                showHeader = true,
                showCategoryFilter = false,
                viewModel = taskViewModel,
            )
        }
        composable(
            route = "lead_task_create/{parentId}/{isLead}",
            arguments = listOf(
                navArgument("parentId") { type = NavType.StringType },
                navArgument("isLead") { type = NavType.BoolType },
            ),
        ) { backStackEntry ->
            val taskViewModel: TaskViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                viewModelStoreOwner = backStackEntry,
            )
            androidx.compose.runtime.LaunchedEffect(Unit) {
                taskViewModel.openCreateSheet()
            }
            TaskScreen(
                showHeader = true,
                showCategoryFilter = false,
                viewModel = taskViewModel,
            )
        }
        composable(
            route = "lead_files/{recordId}",
            arguments = listOf(navArgument("recordId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val attachmentViewModel: AttachmentViewModel = viewModel(backStackEntry)
            AttachmentScreen(
                onBack = { navController.popBackStack() },
                viewModel = attachmentViewModel,
            )
        }
    }
}
