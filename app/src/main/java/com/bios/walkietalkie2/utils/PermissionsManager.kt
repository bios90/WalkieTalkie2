package com.bios.walkietalkie2.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PermissionsManager(private val act: AppCompatActivity) : ForceInitializable {

    private var onGrantedListener: (() -> Unit)? = null
    private var onDeniedListener: (() -> Unit)? = null

    private val permissionsResultListener = act.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(), { resultMap ->
            if (resultMap.all { it.value }) {
                onGrantedListener?.invoke()
            } else {
                onDeniedListener?.invoke()
            }
            onGrantedListener = null
            onDeniedListener = null
        }
    )

    fun checkPermissions(
        onGranted: () -> Unit,
        onDenied: () -> Unit,
        permissions: List<String> = appPermissions,
    ) {
        if (arePermissionsGranted(permissions)) {
            onGranted.invoke()
            onGrantedListener = null
            onDeniedListener = null
            return
        }
        if (onGrantedListener != null || onDeniedListener != null) {
            return
        }
        onGrantedListener = onGranted
        onDeniedListener = onDenied
        permissionsResultListener.launch(permissions.toTypedArray())
    }

    fun arePermissionsGranted() = arePermissionsGranted(appPermissions)

    fun arePermissionsGranted(permissions: List<String>): Boolean = permissions.all {
        ContextCompat.checkSelfPermission(act, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        val appPermissions =
            listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_COARSE_LOCATION)
    }
}