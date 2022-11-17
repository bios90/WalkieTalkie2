package com.bios.walkietalkie2.utils

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class YaCupLocationManager(
    private val activity: AppCompatActivity,
    private val permissionsManager: PermissionsManager,
) {
    private val _flowLocation = MutableSharedFlow<Location>(
        1,
        0
    )
    val flowLocation = _flowLocation.asSharedFlow()
    private var locationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    init {
        activity.addLifeCycleObserver(
            onResume = { startGettingLocation() },
            onPause = { stopGettingLocation() }
        )
    }

    @SuppressLint("MissingPermission")
    private fun startGettingLocation() {
        permissionsManager.checkPermissions(
            permissions = PermissionsManager.locationPermissions,
            onDenied = {},
            onGranted = {
                locationClient = LocationServices.getFusedLocationProviderClient(activity)
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        super.onLocationResult(locationResult)
                        locationResult?.lastLocation?.let(_flowLocation::tryEmit)
                    }
                }
                val request = LocationRequest()
                    .apply {
                        this.interval = MIN_TIME_INTERVAL
                        this.smallestDisplacement = MIN_DISPLACEMENT
                    }
                locationClient?.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper(),
                )
            }
        )
    }

    private fun stopGettingLocation() {
        locationClient?.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val MIN_TIME_INTERVAL = 5000L
        private const val MIN_DISPLACEMENT = 5f

    }
}
