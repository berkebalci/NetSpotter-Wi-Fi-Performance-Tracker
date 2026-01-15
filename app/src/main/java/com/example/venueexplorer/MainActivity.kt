package com.example.venueexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.venueexplorer.presentation.ui.navigation.VenueExplorerNavHost
import com.example.venueexplorer.presentation.ui.theme.VenueExplorerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as VenueExplorerApplication).container
        enableEdgeToEdge()
        setContent {
            VenueExplorerTheme {
                Surface(
                    modifier =  Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VenueExplorerNavHost(appContainer = appContainer)
                }
            }
        }
    }
}

