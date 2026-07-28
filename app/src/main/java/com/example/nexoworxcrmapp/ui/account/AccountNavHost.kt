package com.example.nexoworxcrmapp.ui.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nexoworxcrmapp.ui.task.TaskScreen
import com.example.nexoworxcrmapp.ui.task.TaskViewModel
import androidx.navigation.navArgument
import com.example.nexoworxcrmapp.ui.attachment.AttachmentScreen
import com.example.nexoworxcrmapp.ui.attachment.AttachmentViewModel
import com.example.nexoworxcrmapp.ui.account.ContractListScreen
import com.example.nexoworxcrmapp.ui.account.ContractDetailScreen

private object AccountRoutes {
    const val LIST = "accounts"
    const val CREATE = "account_create"
    const val DETAIL = "account_detail/{accountId}"
    const val EDIT = "account_edit/{accountId}"

    fun detail(accountId: String) = "account_detail/$accountId"
    fun edit(accountId: String) = "account_edit/$accountId"
}

@Composable
fun AccountNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // Tells AccountDetailScreen to reload after returning from Edit
    var refreshDetail by rememberSaveable { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = AccountRoutes.LIST,
        modifier = modifier,
    ) {

        composable(AccountRoutes.LIST) {
            AccountScreen(
                onAccountClick = { account ->
                    navController.navigate(AccountRoutes.detail(account.id))
                },
                onCreateClick = {
                    navController.navigate(AccountRoutes.CREATE)
                },
            )
        }

        composable(AccountRoutes.CREATE) {
            AccountEditScreen(
                onClose = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = AccountRoutes.DETAIL,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
            AccountDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(AccountRoutes.edit(accountId)) },
                refreshTrigger = refreshDetail,
                onRefreshConsumed = { refreshDetail = false },
                viewModel = viewModel(backStackEntry),
                onAddTask = { navController.navigate("account_tasks/$accountId") },
                onFilesClick = { navController.navigate("account_files/$accountId") },
                onContractClick = { navController.navigate("account_contracts/$accountId") },
            )
        }

        composable(
            route = AccountRoutes.EDIT,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType }),
        ) {
            AccountEditScreen(
                onClose = { navController.popBackStack() },
                onSaved = {
                    refreshDetail = true
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = "account_tasks/{parentId}",
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
            route = "account_files/{recordId}",
            arguments = listOf(navArgument("recordId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val attachmentViewModel: AttachmentViewModel = viewModel(backStackEntry)
            AttachmentScreen(
                onBack = { navController.popBackStack() },
                viewModel = attachmentViewModel,
            )
        }
        composable(
            route = "account_contracts/{accountId}",
            arguments = listOf(navArgument("accountId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
            ContractListScreen(
                accountId = accountId,
                onBack = { navController.popBackStack() },
                onContractClick = { contractId ->
                    navController.navigate("contract_detail/$contractId")
                },
            )
        }

        composable(
            route = "contract_detail/{contractId}",
            arguments = listOf(navArgument("contractId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val contractId = backStackEntry.arguments?.getString("contractId") ?: return@composable
            ContractDetailScreen(
                contractId = contractId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
