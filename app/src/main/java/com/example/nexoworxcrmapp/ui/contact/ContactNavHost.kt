// Step 14 of 15
// NEW FILE: app/src/main/java/com/example/nexoworxcrmapp/ui/contact/ContactNavHost.kt

package com.example.nexoworxcrmapp.ui.contact

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private object ContactRoutes {
    const val LIST = "contact_list"
    const val CREATE = "contact_create"
    const val DETAIL = "contact_detail/{contactId}"
    const val EDIT = "contact_edit/{contactId}"

    fun detail(id: String) = "contact_detail/$id"
    fun edit(id: String) = "contact_edit/$id"
}

@Composable
fun ContactNavHost(
    modifier: Modifier = Modifier,
    openCreateOnLaunch: Boolean = false,
    onCreateHandled: () -> Unit = {},
    initialContactId: String? = null,
    onInitialContactHandled: () -> Unit = {},
) {
    val navController = rememberNavController()

    // Deep-link support: Home's Quick Create / Search / Notifications can
    // jump straight into this NavHost without knowing its internal routes.
    androidx.compose.runtime.LaunchedEffect(openCreateOnLaunch) {
        if (openCreateOnLaunch) {
            navController.navigate(ContactRoutes.CREATE) { launchSingleTop = true }
            onCreateHandled()
        }
    }
    androidx.compose.runtime.LaunchedEffect(initialContactId) {
        if (initialContactId != null) {
            navController.navigate(ContactRoutes.detail(initialContactId)) { launchSingleTop = true }
            onInitialContactHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = ContactRoutes.LIST,
        modifier = modifier,
    ) {
        composable(ContactRoutes.LIST) {
            ContactScreen(
                onContactClick = { contact ->
                    navController.navigate(ContactRoutes.detail(contact.id))
                },
                onCreateClick = {
                    navController.navigate(ContactRoutes.CREATE)
                },
            )
        }

        composable(ContactRoutes.CREATE) {
            ContactEditScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = ContactRoutes.DETAIL,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: return@composable
            ContactDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(ContactRoutes.edit(contactId)) },
                onDeleted = { navController.popBackStack(ContactRoutes.LIST, inclusive = false) },
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(backStackEntry),
            )
        }

        composable(
            route = ContactRoutes.EDIT,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
        ) { backStackEntry ->
            ContactEditScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(backStackEntry),
            )
        }
    }
}
