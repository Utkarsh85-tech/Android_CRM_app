package com.example.nexoworxcrmapp.ui.opportunity

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nexoworxcrmapp.ui.attachment.AttachmentScreen
import com.example.nexoworxcrmapp.ui.attachment.AttachmentViewModel
import com.example.nexoworxcrmapp.ui.quote.QuoteCreationViewModel
import com.example.nexoworxcrmapp.ui.quote.QuoteCreationScreen
import com.example.nexoworxcrmapp.ui.quote.QuoteListScreen
import com.example.nexoworxcrmapp.ui.quote.QuoteViewModel
import com.example.nexoworxcrmapp.ui.quote.QuoteWorkspaceScreen
import com.example.nexoworxcrmapp.ui.task.TaskScreen
import com.example.nexoworxcrmapp.ui.task.TaskViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun OpportunityNavHost(
    modifier: Modifier = Modifier,
    openCreateOnLaunch: Boolean = false,
    onCreateHandled: () -> Unit = {},
    pendingVoiceDraft: com.example.nexoworxcrmapp.speech.OpportunityDraft? = null,
    onVoiceDraftConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    var voiceDraftForCreate by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<com.example.nexoworxcrmapp.speech.OpportunityDraft?>(null)
    }

    // Deep-link support: Home's Quick Create can jump straight to the
    // "New opportunity" form without Home needing to know this NavHost's routes.
    androidx.compose.runtime.LaunchedEffect(openCreateOnLaunch) {
        if (openCreateOnLaunch) {
            navController.navigate("opportunity_edit/new")
            onCreateHandled()
        }
    }
    androidx.compose.runtime.LaunchedEffect(pendingVoiceDraft) {
        if (pendingVoiceDraft != null) {
            voiceDraftForCreate = pendingVoiceDraft
            navController.navigate("opportunity_edit/new") { launchSingleTop = true }
            onVoiceDraftConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = "opportunity_list",
        modifier = modifier,
    ) {

        composable("opportunity_list") {
            OpportunityScreen(
                onOpportunityClick = { opp ->
                    navController.navigate("opportunity_detail/${opp.id}?accountId=${opp.accountId}&oppName=${java.net.URLEncoder.encode(opp.name, "UTF-8")}&accountName=${java.net.URLEncoder.encode(opp.accountName, "UTF-8")}")                },
                onCreateClick = {
                    navController.navigate("opportunity_edit/new")
                },
            )
        }

        composable(
            route = "opportunity_detail/{opportunityId}?accountId={accountId}&oppName={oppName}&accountName={accountName}",            arguments = listOf(
                navArgument("opportunityId") { type = NavType.StringType },
                navArgument("accountId") { type = NavType.StringType; defaultValue = "" },
                navArgument("oppName") { type = NavType.StringType; defaultValue = "" },
                navArgument("accountName") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val opportunityId = backStackEntry.arguments?.getString("opportunityId") ?: return@composable
            val accountId = backStackEntry.arguments?.getString("accountId").orEmpty()
            val oppName = backStackEntry.arguments?.getString("oppName").orEmpty()
            val accountName = backStackEntry.arguments?.getString("accountName").orEmpty()
            OpportunityDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate("opportunity_edit/$opportunityId") },
                onDeleted = { navController.popBackStack("opportunity_list", inclusive = false) },
                onAddTask = { navController.navigate("opportunity_tasks/$opportunityId") },
                onFilesClick = { navController.navigate("opportunity_files/$opportunityId") },
                onQuoteClick = {
                    navController.navigate(
                        "quote_list/$opportunityId?accountId=$accountId&oppName=${java.net.URLEncoder.encode(oppName, "UTF-8")}&accountName=${java.net.URLEncoder.encode(accountName, "UTF-8")}"                    )
                },
                viewModel = viewModel(),
            )
        }

        composable(
            route = "opportunity_edit/{opportunityId}",
            arguments = listOf(navArgument("opportunityId") { type = NavType.StringType }),
        ) {
            OpportunityEditScreen(
                onBack = { voiceDraftForCreate = null; navController.popBackStack() },
                onSaved = { voiceDraftForCreate = null; navController.popBackStack() },
                viewModel = viewModel(),
                voicePrefill = voiceDraftForCreate,
            )
        }

        composable(
            route = "opportunity_tasks/{parentId}",
            arguments = listOf(navArgument("parentId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val taskViewModel: TaskViewModel = viewModel(backStackEntry)
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
            route = "opportunity_files/{recordId}",
            arguments = listOf(navArgument("recordId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val attachmentViewModel: AttachmentViewModel = viewModel(backStackEntry)
            AttachmentScreen(
                onBack = { navController.popBackStack() },
                viewModel = attachmentViewModel,
            )
        }

        composable(
            route = "quote_list/{opportunityId}?accountId={accountId}&oppName={oppName}&accountName={accountName}",
            arguments = listOf(
                navArgument("opportunityId") { type = NavType.StringType },
                navArgument("accountId") { type = NavType.StringType; defaultValue = "" },
                navArgument("oppName") { type = NavType.StringType; defaultValue = "" },
                navArgument("accountName") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val opportunityId = backStackEntry.arguments?.getString("opportunityId") ?: return@composable
            val accountId = backStackEntry.arguments?.getString("accountId").orEmpty()
            val oppName = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("oppName").orEmpty(), "UTF-8")
            val accountName = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("accountName").orEmpty(), "UTF-8")
            QuoteListScreen(
                opportunityId = opportunityId,
                accountId = accountId,
                onBack = { navController.popBackStack() },
                onCreateQuote = {
                    navController.navigate(
                        "quote_create/$opportunityId?accountId=$accountId&oppName=${java.net.URLEncoder.encode(oppName, "UTF-8")}&accountName=${java.net.URLEncoder.encode(accountName, "UTF-8")}"
                    )
                },
                onOpenQuote = { quoteId ->
                    navController.navigate("quote_workspace/$quoteId")
                },
            )
        }

        // Quote creation — calls API, then navigates to workspace with real quoteId
        composable(
            route = "quote_create/{opportunityId}?accountId={accountId}&oppName={oppName}&accountName={accountName}",            arguments = listOf(
                navArgument("opportunityId") { type = NavType.StringType },
                navArgument("accountId") { type = NavType.StringType; defaultValue = "" },
                navArgument("oppName") { type = NavType.StringType; defaultValue = "" },
                navArgument("accountName") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val opportunityId = backStackEntry.arguments?.getString("opportunityId") ?: return@composable
            val accountId = backStackEntry.arguments?.getString("accountId").orEmpty()
            val oppName = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("oppName").orEmpty(), "UTF-8")
            val accountName = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("accountName").orEmpty(), "UTF-8")

            val creationViewModel: QuoteCreationViewModel = viewModel(
                key = "create_$opportunityId",
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(
                        modelClass: Class<T>,
                        extras: androidx.lifecycle.viewmodel.CreationExtras,
                    ): T {
                        val handle = SavedStateHandle(mapOf(
                            "opportunityId" to opportunityId,
                            "accountId" to accountId,
                            "opportunityName" to oppName,
                            "accountName" to accountName,                        ))
                        @Suppress("UNCHECKED_CAST")
                        return QuoteCreationViewModel(handle) as T
                    }
                }
            )
            QuoteCreationScreen(
                onQuoteCreated = { quoteId ->
                    navController.navigate("quote_workspace/$quoteId") {
                        // Remove creation screen from back stack
                        popUpTo("quote_create/$opportunityId?accountId=$accountId&oppName=${java.net.URLEncoder.encode(oppName, "UTF-8")}") {
                            inclusive = true
                        }
                    }
                },
                onBack = { navController.popBackStack() },
                viewModel = creationViewModel,
            )
        }

        // Quote workspace — actual quote editing
        composable(
            route = "quote_workspace/{quoteId}",
            arguments = listOf(navArgument("quoteId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val quoteId = backStackEntry.arguments?.getString("quoteId") ?: return@composable
            val quoteViewModel: QuoteViewModel = viewModel(
                key = "workspace_$quoteId",
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(
                        modelClass: Class<T>,
                        extras: androidx.lifecycle.viewmodel.CreationExtras,
                    ): T {
                        val handle = SavedStateHandle(mapOf("quoteId" to quoteId))
                        @Suppress("UNCHECKED_CAST")
                        return QuoteViewModel(handle) as T
                    }
                }
            )
            QuoteWorkspaceScreen(
                onBack = { navController.popBackStack() },
                viewModel = quoteViewModel,
            )
        }
    }
}