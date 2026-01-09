package com.example.venueexplorer.presentation.ui.navigation

sealed class VenueExplorerNavDestination(val route: String) {
    data object Home: VenueExplorerNavDestination("HomeScreen") //object keywordu oldugu zaman tum projede sadece bir adet olusturuluyor.
    data object Edit: VenueExplorerNavDestination("EditScreen/{venueId}"){
        fun createRoute(venueId: String) = "EditScreen/$venueId"
    }
    data object Add : VenueExplorerNavDestination("AddScreen")
    data object Details: VenueExplorerNavDestination("DetailsScreen/{venueId}"){
        fun createRoute(venueId: String) = "DetailsScreen/$venueId"
    }


}