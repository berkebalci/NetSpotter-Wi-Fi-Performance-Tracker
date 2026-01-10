package com.example.venueexplorer.presentation.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.venueexplorer.data.local.AppContainer

import com.example.venueexplorer.di.VenueExplorerViewModelFactory
import com.example.venueexplorer.presentation.home.HomeScreen
import com.example.venueexplorer.presentation.ui.details.DetailsScreen
import com.example.venueexplorer.presentation.ui.edit.EditScreen

@Composable
fun VenueExplorerNavHost(
    navController: NavHostController = rememberNavController(),
    appContainer: AppContainer
) {
    val viewModelFactory = VenueExplorerViewModelFactory(
        venueRepository = appContainer.venueLocalRepository,
        categoryRepository = appContainer.categoryRepository
    )
    NavHost(
        navController = navController,
        startDestination = VenueExplorerNavDestination.Home.route
    ){
        composable(route = VenueExplorerNavDestination.Home.route) {
            HomeScreen(
                viewModel = viewModel(factory = viewModelFactory),
                onNavigateToDetailsScreen = { venueId ->
                    navController.navigate(VenueExplorerNavDestination.Details.createRoute(venueId))
                },
                onNavigateToEditScreen = { venueId ->
                    navController.navigate(VenueExplorerNavDestination.Edit.createRoute(venueId = venueId!!))
                },
                onNavigateToAddVenue = {
                    // FAB'a basınca Add rotasına git
                    navController.navigate(VenueExplorerNavDestination.Add.route)
                }

            )

        }
        composable(route = VenueExplorerNavDestination.Details.route,
            arguments = listOf(
                navArgument("venueId") {
                    type = NavType.StringType
                })) { backStackEntry ->
            val venueId = backStackEntry.arguments?.getString("venueId") ?: return@composable
            Log.e("Navigation Sirasinda", "navigation VenueId is : $venueId")
            DetailsScreen(
                venueId = venueId,
                viewModel = viewModel(factory = viewModelFactory),
                onBackClicked =  {
                    navController.popBackStack()
                },
            onEditButtonClicked =  { venueId->
                navController.navigate(VenueExplorerNavDestination.Edit.createRoute(venueId))
            },
            onMapContainerClicked = {

            },
            )
        }
        composable(route = VenueExplorerNavDestination.Add.route) {
            EditScreen(
                venueId = null, // ID göndermiyoruz!
                viewModel = viewModel(factory = viewModelFactory),
                onSaveButtonClicked = { navController.popBackStack() },
                onCancelButtonClicked = { navController.popBackStack() }
            )
        }
        composable(route = VenueExplorerNavDestination.Edit.route,
            arguments = listOf(
                navArgument("venueId") {
                    type = NavType.StringType
                })) { backStackEntry ->
            val venueId = backStackEntry.arguments?.getString("venueId") ?: return@composable
            Log.e("Navigation Sirasinda", "navigation VenueId is : $venueId")
            EditScreen(
                venueId = venueId
                ,viewModel = viewModel(factory = viewModelFactory),
                onSaveButtonClicked ={
                    navController.popBackStack()
                } ,
            onCancelButtonClicked = {
                navController.popBackStack()
            },

            )
        }
    }
}