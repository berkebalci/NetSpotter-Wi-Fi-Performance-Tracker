package com.example.venueexplorer.presentation.ui.navigation

sealed class NavDestination(route: String) {
    data object Home: NavDestination("home") //object keywordu oldugu zaman tum projede sadece bir adet olusturuluyor.
    data object Edit: NavDestination("edit")
    data object Details: NavDestination("details/{venueId}"){
        fun createRoute(venueId: String) = "details/$venueId"
    }


}