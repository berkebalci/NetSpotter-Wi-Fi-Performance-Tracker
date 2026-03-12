package com.example.venueexplorer.fakes

import android.location.Location
import com.example.venueexplorer.domain.repository.LocationRepository
import com.google.android.gms.tasks.Task


class FakeLocationRepository : LocationRepository
//su an biz bunu mockladik.
{
    override fun hasLocationPermission(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLastLocation(): Task<Location?> {
        TODO("Not yet implemented")
    }

    override fun getCurrentLocation(): Task<Location> {
        TODO("Not yet implemented")
    }

    override fun requestSingleLocationUpdate(
        onLocationReceived: (Location?) -> Unit,
        timeoutMillis: Long
    ) {
        TODO("Not yet implemented")
    }
}