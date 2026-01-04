package com.example.venueexplorer

import android.app.Application
import com.example.venueexplorer.data.local.AppContainer

class VenueExplorerApplication : Application() {
   lateinit var container: AppContainer

   override fun onCreate() {
      super.onCreate()
      container = AppContainer(this)
   }
}
