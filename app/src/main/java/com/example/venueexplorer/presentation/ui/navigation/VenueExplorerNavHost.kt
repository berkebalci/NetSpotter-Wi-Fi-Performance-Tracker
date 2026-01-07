package com.example.venueexplorer.presentation.ui.navigation

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
                onNavigateToDetailsScreen = {
                    navController.navigate(VenueExplorerNavDestination.Details.route)
                },
                onNavigateToEditScreen = {
                    navController.navigate(VenueExplorerNavDestination.Edit.route)
                }
            )
        }
        composable(route = VenueExplorerNavDestination.Details.route,
            arguments = listOf(
                navArgument("venueId") {
                    type = NavType.StringType
                })) { backStackEntry ->
            val venueId = backStackEntry.arguments?.getString("venueId") ?: return@composable

            DetailsScreen(
                venueId = venueId,
                viewModel = viewModel(factory = viewModelFactory),
                onBackClicked =  {
                    navController.popBackStack()
                },
            onEditButtonClicked =  {
                navController.navigate(VenueExplorerNavDestination.Edit.route)
            },
            onMapContainerClicked = {

            },
            )
        }
        composable(route = VenueExplorerNavDestination.Edit.route){
            EditScreen(
                viewModel = viewModel(factory = viewModelFactory),
                onSaveButtonClicked ={} ,
            onCancelButtonClicked = {},

            )
        }
    }
}